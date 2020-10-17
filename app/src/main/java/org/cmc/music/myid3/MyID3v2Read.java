/*
 * Written By Charles M. Chen
 *
 * Created on Sep 2, 2005
 *
 */

package org.cmc.music.myid3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class MyID3v2Read implements MyID3v2Constants {

    private static final int kHIGH_BIT = 1 << 7;

    private final InputStream is;
    private final boolean async;
    private final MyID3Listener listener;

    public MyID3v2Read(final MyID3Listener listener, final InputStream is,
                       boolean async) {
        this.listener = listener;
        this.is = is;
        this.async = async;
    }

    // private boolean verbose = false;
    //
    // public void setVerbose()
    // {
    // verbose = true;
    // }

    private boolean complete = false, error = false, no_tag = false,
            stream_complete = false;

    //
    // private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // private long bytes_read = 0;
    // private int tagLength = 0;
    // private final byte buffer[] = new byte[1024];

    public void dump() {


    }

    public boolean isComplete() {
        return complete || error || no_tag;
    }

    public boolean isError() {
        return error;
    }

    public boolean hasTags() {
        return !error && complete && !no_tag;
    }

    // public boolean isSuccess()
    // {
    // return tag_read && !error;
    // }

    private boolean header_read = false, tagRead = false;
    private int index = 0, last = -1;

    public boolean iteration() {
        if (isComplete())
            return true;

        if (!read())
            return false;

        if (isComplete())
            return true;

        if (!header_read) {
            if (bytes_read < TAG_HEADER_LENGTH) {
                if (stream_complete)
                    error = true;
                return true;
            }
            readHeader();
        }

        if (!tagRead) {
            if (bytes_read < tagLength) {
                if (stream_complete)
                    error = true;
                return true;
            }
            readTag();

            complete = true;


            // if (file_precis != null)
            // {
            // file_precis.setID3v2(getBytes(), getTags());
            // }
        }

        // complete = true;

        return true;
    }

    private int readInt3(byte bytes[], boolean check_tagLength) {
        if (((index + 2) >= tagLength) && check_tagLength) {
            setError("readInt3(index: " + index + ", tagLength: " + tagLength);
            return -1;
        }
        if ((index + 3) >= bytes.length) {
            setError("readInt3(index: " + index + ", bytes.length: "
                    + bytes.length);
            return -1;
        }

        int array[] = {0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
        };

        return (array[0] << 16) | (array[1] << 8) | (array[2] << 0);
    }

    public static Number readSynchsafeInt(byte bytes[], int start) {
        if ((start + 3) >= bytes.length) {
            // setError("readSynchsafeInt(index: " + start + ", bytes.length: "
            // + bytes.length);
            return null;
        }

        int index = start;
        int array[] = {0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
        };

        for (int i = 0; i < array.length; i++) {
            if ((array[i] & kHIGH_BIT) > 0) {
                array[i] &= kHIGH_BIT;
            }
        }

        return (array[0] << 21) | (array[1] << 14) | (array[2] << 7)
                | (array[3] << 0);
    }

    private int readSynchsafeInt(byte bytes[], boolean check_tagLength) {
        if (((index + 3) >= tagLength) && check_tagLength) {
            setError("readSynchsafeInt(index: " + index + ", tagLength: "
                    + tagLength);
            return -1;
        }
        if ((index + 3) >= bytes.length) {
            setError("readSynchsafeInt(index: " + index + ", bytes.length: "
                    + bytes.length);
            return -1;
        }

        int array[] = {0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
        };


        for (int i = 0; i < array.length; i++) {

            if ((array[i] & kHIGH_BIT) > 0) {
                array[i] &= kHIGH_BIT;
            }
        }

        int result = (array[0] << 21) | (array[1] << 14) | (array[2] << 7)
                | (array[3] << 0);
        return result;
    }

    private int readInt(byte bytes[], boolean check_tagLength) {
        if (((index + 3) >= tagLength) && check_tagLength) {
            setError("readInt(index: " + index + ", tagLength: " + tagLength);
            return -1;
        }
        if ((index + 3) >= bytes.length) {
            setError("readInt(index: " + index + ", bytes.length: "
                    + bytes.length);
            return -1;
        }

        int array[] = {0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
                0xff & bytes[index++], //
        };

        int result = (array[0] << 24) | (array[1] << 16) | (array[2] << 8)
                | (array[3] << 0);
        return result;
    }


    private int readShort(byte bytes[]) {
        if (((index + 1) >= tagLength) || ((index + 1) >= bytes.length)) {
            setError("readShort(index: " + index + ", tagLength: " + tagLength
                    + ", bytes.length: " + bytes.length);


            return -1;
        }
        byte array[] = {bytes[index++], //
                bytes[index++], //
        };

        int result = (array[0] << 8) | (array[1] << 0);
        return result;
    }

    private byte versionMajor, versionMinor;
    private boolean tagUnsynchronization = false, tagCompression = false,
            tagExtendedHeader = false, tagExperimentalIndicator = false,
            tagFooterPresent = false;

    private void readHeader() {
        byte bytes[] = baos.toByteArray();
        if (bytes.length < 10) {
            setError("missing header");
            return;
        }

        if (listener != null)
            listener.log("id3v2 header");

        if (bytes[index++] != 0x49)
            no_tag = true;
        else if (bytes[index++] != 0x44)
            no_tag = true;
        else if (bytes[index++] != 0x33)
            no_tag = true;

        if (error || no_tag)
            return;

        versionMajor = bytes[index++];
        versionMinor = bytes[index++];

        if (listener != null) {
            listener.log("\t" + "id3v2 versionMajor", versionMajor);
            listener.log("\t" + "id3v2 versionMinor", versionMinor);
        }

        if ((versionMajor < 2) || (versionMajor > 4)) {
            setError("Unknown id3v2 Major Version: " + versionMajor);
            return;
        }
        long flags = bytes[index++];
        long workingFlags = flags;

        if (versionMajor == 2) {
            if ((workingFlags & HEADER_FLAG_ID3v22_UNSYNCHRONISATION) > 0) {
                tagUnsynchronization = true;
                workingFlags ^= HEADER_FLAG_ID3v22_UNSYNCHRONISATION;
            }
            if ((workingFlags & HEADER_FLAG_ID3v22_COMPRESSION) > 0) {
                tagCompression = true;
                workingFlags ^= HEADER_FLAG_ID3v22_COMPRESSION;
            }
        } else if (versionMajor == 3) {
            if ((workingFlags & HEADER_FLAG_ID3v23_UNSYNCHRONISATION) > 0) {
                tagUnsynchronization = true;
                workingFlags ^= HEADER_FLAG_ID3v23_UNSYNCHRONISATION;
            }
            if ((workingFlags & HEADER_FLAG_ID3v23_EXTENDED_HEADER) > 0) {
                tagExtendedHeader = true;
                workingFlags ^= HEADER_FLAG_ID3v23_EXTENDED_HEADER;
            }
            if ((workingFlags & HEADER_FLAG_ID3v23_EXPERIMENTAL_INDICATOR) > 0) {
                tagExperimentalIndicator = true;
                workingFlags ^= HEADER_FLAG_ID3v23_EXPERIMENTAL_INDICATOR;
            }

            // hack to fix old mistake.
            if ((workingFlags & HEADER_FLAG_ID3v24_FOOTER_PRESENT) > 0)
                workingFlags ^= HEADER_FLAG_ID3v24_FOOTER_PRESENT;

        } else if (versionMajor == 4) {
            if ((workingFlags & HEADER_FLAG_ID3v24_UNSYNCHRONISATION) > 0) {
                tagUnsynchronization = true;
                workingFlags ^= HEADER_FLAG_ID3v24_UNSYNCHRONISATION;
            }
            if ((workingFlags & HEADER_FLAG_ID3v24_EXTENDED_HEADER) > 0) {
                tagExtendedHeader = true;
                workingFlags ^= HEADER_FLAG_ID3v24_EXTENDED_HEADER;
            }
            if ((workingFlags & HEADER_FLAG_ID3v24_EXPERIMENTAL_INDICATOR) > 0) {
                tagExperimentalIndicator = true;
                workingFlags ^= HEADER_FLAG_ID3v24_EXPERIMENTAL_INDICATOR;
            }
            if ((workingFlags & HEADER_FLAG_ID3v24_FOOTER_PRESENT) > 0) {
                tagFooterPresent = true;
                workingFlags ^= HEADER_FLAG_ID3v24_FOOTER_PRESENT;
            }
        } else {
            setError("Unknown id3v2 Major Version: " + versionMajor);
            return;
        }
        if (workingFlags > 0) {
            setError("Unknown id3v2 tag flags(id3v2 version: " + versionMajor
                    + "): " + Long.toHexString(flags));
            return;
        }

        if (listener != null) {
            listener.log("\t" + "unsynchronization", tagUnsynchronization);
            listener.log("\t" + "compression", tagCompression);
            listener.log("\t" + "extendedHeader", tagExtendedHeader);
            listener.log("\t" + "experimentalIndicator",
                    tagExperimentalIndicator);
            listener.log("\t" + "footerPresent", tagFooterPresent);
        }

        {
            tagLength = readSynchsafeInt(bytes, false);

            tagLength += 10;
            last = tagLength;
            if (tagFooterPresent)
                tagLength += 10;
        }

        header_read = true;
        if (index != TAG_HEADER_LENGTH)
            setError("index!=kHEADER_SIZE");

        if (listener != null) {
            listener.log("\t" + "tagLength", tagLength);
            listener.log();
        }
    }

    // private boolean extended_header;
    private final Vector tags = new Vector();

    private byte[] ununsynchronize(byte bytes[]) {


        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int i = 0;
        for (; i < bytes.length; ) {
            byte b = bytes[i++];
            result.write(b);
            if ((0xff & b) != 0xff)
                continue;

            if (i >= bytes.length)
                break;

            // look ahead.
            byte b1 = bytes[i];
            if ((0xff & b1) == 0)
                i++;
        }
        bytes = result.toByteArray();

        return bytes;
    }

    private static final String LEGAL_FRAME_ID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String parseFrameID(byte bytes[]) {
        for (byte aByte : bytes) {
            int b = 0xff & aByte;
            char c = (char) b;
            if (LEGAL_FRAME_ID_CHARACTERS.indexOf(c) < 0) {
                setError("invalid id3v2 frame id byte: "
                        + Integer.toHexString(b));
                return null;
            }
        }
        return new String(bytes);
    }

    private boolean isZeroFrameId(byte bytes[]) {
        for (byte aByte : bytes) {
            if ((0xff & aByte) > 0)
                return false;
        }
        return true;
    }

    private void readTag() {
        byte bytes[] = baos.toByteArray();
        if (bytes.length < tagLength) {
            setError("missing tag");
            return;
        }

        if (tagUnsynchronization)
            bytes = ununsynchronize(bytes);

        if (tagExtendedHeader)
            index += 4;

        int tagCount = 0;
        for (int tag_num = 0; (((index + 7) < last) && (!error)); tag_num++) {
            if ((index + 7) >= last)
                break;

            byte frameID[];

            if (versionMajor >= 3) {
                frameID = new byte[]{bytes[index++], //
                        bytes[index++], //
                        bytes[index++], //
                        bytes[index++], //
                };
            } else {
                frameID = new byte[]{bytes[index++], //
                        bytes[index++], //
                        bytes[index++], //
                };
            }
            if (isZeroFrameId(frameID)) {
                // Not a frame, rest of the tag is padding.
                if (listener != null)
                    listener.log("zero frameID", frameID);
                break;
            }
            String frameIDString = parseFrameID(frameID);
            if (null == frameIDString)
                break;

            if (listener != null)
                listener.log("id3v2 frameIDString", frameIDString);

            int frameLength;
            if (versionMajor >= 4)
                frameLength = readSynchsafeInt(bytes, true);
            else if (versionMajor >= 3)
                frameLength = readInt(bytes, true);
            else
                frameLength = readInt3(bytes, true);

            if (listener != null)
                listener.log("frameLength", frameLength);

            int maxTagLength = tagLength - index;
            if (versionMajor >= 3)
                maxTagLength += 2;

            if (frameLength == 0) {
                if (listener != null)
                    listener.log("frame has zero length.");
                break;
            }

            if ((frameLength > maxTagLength) || (frameLength < 0)) {
                if (listener != null) {
                    listener
                            .log("frame length exceeds tag length", frameLength);
                    listener.log("bad frameLength versionMajor", versionMajor);
                    listener.log("bad frameLength versionMinor", versionMinor);
                    listener
                            .log("bad frameLength frameIDString", frameIDString);
                    listener.log("bad frameLength maxTagLength", maxTagLength);
                    listener.log("bad frameLength frameLength", frameLength
                            + " (0x" + Integer.toHexString(frameLength) + ")");
                    listener.log("bad frameLength tagLength", tagLength
                            + " (0x" + Integer.toHexString(tagLength) + ")");
                    listener.log("bad frameLength index", index);

                    listener.log("bytes", bytes);
                }

                setError("bad frame length(" + tag_num + ": " + frameIDString
                        + "): " + frameLength + " (" + new String(frameID));

                break;
            }

            ID3v2FrameFlags flags = null;
            if (versionMajor == 3 || versionMajor == 4) {
                int value = readShort(bytes);
                int workingFlags = value;

                flags = new ID3v2FrameFlags();

                if (versionMajor == 3) {
                    if ((workingFlags & FRAME_FLAG_ID3v23_TAG_ALTER_PRESERVATION) > 0) {
                        flags.setTagAlterPreservation(true);
                        workingFlags ^= FRAME_FLAG_ID3v23_TAG_ALTER_PRESERVATION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v23_FILE_ALTER_PRESERVATION) > 0) {
                        flags.setFileAlterPreservation(true);
                        workingFlags ^= FRAME_FLAG_ID3v23_FILE_ALTER_PRESERVATION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v23_READ_ONLY) > 0) {
                        flags.setReadOnly(true);
                        workingFlags ^= FRAME_FLAG_ID3v23_READ_ONLY;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v23_GROUPING_IDENTITY) > 0) {
                        flags.setGroupingIdentity(true);
                        workingFlags ^= FRAME_FLAG_ID3v23_GROUPING_IDENTITY;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v23_COMPRESSION) > 0) {
                        flags.setCompression(true);
                        workingFlags ^= FRAME_FLAG_ID3v23_COMPRESSION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v23_ENCRYPTION) > 0) {
                        flags.setEncryption(true);
                        workingFlags ^= FRAME_FLAG_ID3v23_ENCRYPTION;
                    }
                } else if (versionMajor == 4) {

                    if ((workingFlags & FRAME_FLAG_ID3v24_TAG_ALTER_PRESERVATION) > 0) {
                        flags.setTagAlterPreservation(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_TAG_ALTER_PRESERVATION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_FILE_ALTER_PRESERVATION) > 0) {
                        flags.setFileAlterPreservation(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_FILE_ALTER_PRESERVATION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_READ_ONLY) > 0) {
                        flags.setReadOnly(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_READ_ONLY;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_GROUPING_IDENTITY) > 0) {
                        flags.setGroupingIdentity(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_GROUPING_IDENTITY;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_COMPRESSION) > 0) {
                        flags.setCompression(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_COMPRESSION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_ENCRYPTION) > 0) {
                        flags.setEncryption(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_ENCRYPTION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_UNSYNCHRONISATION) > 0) {
                        flags.setUnsynchronisation(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_UNSYNCHRONISATION;
                    }
                    if ((workingFlags & FRAME_FLAG_ID3v24_DATA_LENGTH_INDICATOR) > 0) {
                        flags.setDataLengthIndicator(true);
                        workingFlags ^= FRAME_FLAG_ID3v24_DATA_LENGTH_INDICATOR;
                    }
                }

                if (workingFlags > 0) {
                    setError("Unknown id3v2 frame flags(id3v2 version: "
                            + versionMajor + "): " + Long.toHexString(value));
                    return;
                }
            } else if (versionMajor == 2) {
                flags = new ID3v2FrameFlags();
            } else {
                setError("Unknown ID3v2 version: " + versionMajor);
                return;
            }

            if (listener != null)
                listener.log("flags", flags.getSummary());

            if (frameLength > 0) {
                int dataLengthIndicator = -1;
                if (flags != null && flags.getDataLengthIndicator()) {
                    dataLengthIndicator = readSynchsafeInt(bytes, true);
                    frameLength -= 4;
                    if (listener != null)
                        listener
                                .log("dataLengthIndicator", dataLengthIndicator);
                }

                byte frameBytes[] = new byte[frameLength];

                System.arraycopy(bytes, index, frameBytes, 0, frameLength);
                index += frameLength;

                if (flags != null && flags.getUnsynchronisation())
                    frameBytes = ununsynchronize(frameBytes);

                try {
                    if (frameID[0] == 'T') {
                        if (listener != null)
                            listener.log("text frame");
                        readTextTag(frameLength, frameID, frameBytes,
                                frameIDString);
                    } else {
                        if (listener != null)
                            listener.log("data frame");
                        readDataTag(frameLength, frameID, frameBytes,
                                frameIDString, flags);
                    }
                } catch (IOException e) {
                    if (listener != null)
                        listener.log("IOException", e.getMessage());
                    setError(e.getMessage());

                    // TODO: return or break here or what?
                    return;
                }
            }
            tagCount++;

            if (listener != null)
                listener.log();
        }

        tagRead = true;

        if (listener != null)
            listener.log();

    }

    private void readDataTag(int frameLength, byte frameID[],
                             byte frameBytes[], String frameIDString, ID3v2FrameFlags flags)
            throws IOException {
        switch (frameIDString) {
            case "COMM":
            case "COM": {
                if (frameBytes.length < 5) {
                    setError("Unexpected COMM frame length(1): " + frameLength
                            + " (" + new String(frameID));
                    return;
                }
                int frameIndex = 0;
                int charEncodingCode = 0xff & frameBytes[frameIndex++];
                byte language_1 = frameBytes[frameIndex++];
                byte language_2 = frameBytes[frameIndex++];
                byte language_3 = frameBytes[frameIndex++];

                String summary = readString(frameBytes, frameIndex,
                        charEncodingCode);

                int stringDataLength = findStringDataLength(frameBytes, frameIndex,
                        charEncodingCode);
                frameIndex += stringDataLength;

                String comment;
                comment = readString(frameBytes, frameIndex, charEncodingCode);

                MyID3v2FrameText tag = new MyID3v2FrameText(frameIDString,
                        frameBytes, comment);
                tags.add(tag);
                break;
            }
            case "PIC":
            case "APIC": {
                int frameIndex = 0;
                int charEncodingCode = 0xff & frameBytes[frameIndex++];

                String mimeType;
                if (frameIDString.equals("PIC")) {
                    int imageFormat1 = 0xff & frameBytes[frameIndex++];
                    int imageFormat2 = 0xff & frameBytes[frameIndex++];
                    int imageFormat3 = 0xff & frameBytes[frameIndex++];

                    String extension = "" + (char) imageFormat1
                            + (char) imageFormat2 + (char) imageFormat3;

                    mimeType = extension.toLowerCase();
                    if (!mimeType.startsWith("image/"))
                        mimeType = "image/" + mimeType;
                } else {
                    mimeType = readString(frameBytes, frameIndex, charEncodingCode);

                    int stringDataLength = findStringDataLength(frameBytes,
                            frameIndex, charEncodingCode);
                    frameIndex += stringDataLength;
                }


                int pictureType = 0xff & frameBytes[frameIndex++];


                String description;
                {
                    description = readString(frameBytes, frameIndex,
                            charEncodingCode);

                    int stringDataLength = findStringDataLength(frameBytes,
                            frameIndex, charEncodingCode);
                    frameIndex += stringDataLength;
                }
                byte imageData[] = new byte[frameBytes.length - frameIndex];
                System.arraycopy(frameBytes, frameIndex, imageData, 0,
                        imageData.length);

                tags.add(new MyID3v2FrameImage(frameIDString, frameBytes, flags,
                        imageData, mimeType, description, pictureType));
                break;
            }
            case "PRIV": {
                int frameIndex = 0;
                String owner_identifier;
                {
                    byte charEncodingCode = CHAR_ENCODING_CODE_ISO_8859_1;
                    owner_identifier = readString(frameBytes, frameIndex,
                            charEncodingCode);

                    int stringDataLength = findStringDataLength(frameBytes,
                            frameIndex, charEncodingCode);
                    frameIndex += stringDataLength;
                }
                if (owner_identifier.startsWith("WM/"))
                    return;

                break;
            }
            default:
                tags.add(new MyID3v2FrameData(frameIDString, frameBytes, flags));
                break;
        }
    }

    private void readTextTag(int frameLength, byte frameID[],
                             byte frameBytes[], String frameIDString) throws IOException {
        if (frameLength == 1) {
        } else if (frameLength < 2) {
            setError("Unexpected frame length(1): " + frameLength + " ("
                    + new String(frameID));
        } else {
            int charEncodingCode = 0xff & frameBytes[0];

            int frameIndex = 1;
            String value = readString(frameBytes, frameIndex, charEncodingCode);

            if (listener != null)
                listener.logWithLength("value", value);

            MyID3v2FrameText tag;

            String value2 = null;
            if (frameIDString.equals("TXXX")) {
                int stringDataLength = findStringDataLength(frameBytes,
                        frameIndex, charEncodingCode);
                frameIndex += stringDataLength;

                value2 = readString(frameBytes, frameIndex, charEncodingCode);

                if (listener != null)
                    listener.logWithLength("value2", value2);

                tag = new MyID3v2FrameText(frameIDString, frameBytes, value,
                        value2);
            } else
                tag = new MyID3v2FrameText(frameIDString, frameBytes, value);

            tags.add(tag);
        }
    }

    private String getCharacterEncodingName(int charEncodingCode)
            throws IOException {
        switch (charEncodingCode) {
            case CHAR_ENCODING_CODE_ISO_8859_1:
                return CHAR_ENCODING_ISO;
            case CHAR_ENCODING_CODE_UTF_16_WITH_BOM:
                return CHAR_ENCODING_UTF_16;
            case CHAR_ENCODING_CODE_UTF_16_NO_BOM:
                return CHAR_ENCODING_UTF_16;
            case CHAR_ENCODING_CODE_UTF_8:
                return CHAR_ENCODING_UTF_8;
            default:
                throw new IOException("Unknown charEncodingCode: "
                        + charEncodingCode);
        }
    }

    private String getCharacterEncodingFullName(int charEncodingCode)
            throws IOException {
        switch (charEncodingCode) {
            case CHAR_ENCODING_CODE_ISO_8859_1:
                return CHAR_ENCODING_ISO;
            case CHAR_ENCODING_CODE_UTF_16_WITH_BOM:
                return CHAR_ENCODING_UTF_16_WITH_BOM;
            case CHAR_ENCODING_CODE_UTF_16_NO_BOM:
                return CHAR_ENCODING_UTF_16_WITHOUT_BOM;
            case CHAR_ENCODING_CODE_UTF_8:
                return CHAR_ENCODING_UTF_8;
            default:
                throw new IOException("Unknown charEncodingCode: "
                        + charEncodingCode);
        }
    }

    private String readString(byte bytes[], int start, int charEncodingCode)
            throws IOException {
        if (listener != null)
            listener.log("reading string with encoding",
                    getCharacterEncodingFullName(charEncodingCode));

        UnicodeMetrics unicodeMetrics = UnicodeMetrics
                .getInstance(charEncodingCode);
        int unicodeMetricsEnd = unicodeMetrics.findEndWithoutTerminator(bytes,
                start);
        int unicodeMetricsLength = unicodeMetricsEnd - start;

        String charsetName = getCharacterEncodingName(charEncodingCode);
        return new String(bytes, start, unicodeMetricsLength, charsetName);
    }

    private int findStringDataLength(byte bytes[], int start,
                                     int charEncodingCode) throws IOException {
        UnicodeMetrics unicodeMetrics = UnicodeMetrics
                .getInstance(charEncodingCode);
        int unicodeMetricsEnd = unicodeMetrics.findEndWithTerminator(bytes,
                start);
        int unicodeMetricsLength = unicodeMetricsEnd - start;
        return unicodeMetricsLength;
    }

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private long bytes_read = 0;
    private int tagLength = 0;
    private final byte buffer[] = new byte[1024];

    private boolean read() {
        try {
            if (is.available() < 0) {
                stream_complete = true;
                return true;
            }
            if (!async && is.available() < 1) {
                stream_complete = true;
                return true;
            }

            if (is.available() < 1)
                return false;

            {
                int read = is.read(buffer);
                if (read < 1) {
                    setError("unexpected stream closed");
                    return true;
                }

                baos.write(buffer, 0, read);
                bytes_read += read;
            }

            return true;
        } catch (IOException e) {

            setError(e.getMessage());
            return true;
        }
    }

    private String errorMessage = null;

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setError(String s) {
        error = true;

        errorMessage = s;
    }

    public Vector getTags() {
        return tags;
    }

    public byte getVersionMajor() {
        return versionMajor;
    }

    public byte getVersionMinor() {
        return versionMinor;
    }

    public long getProgress() {
        return bytes_read;
    }

    public byte[] getBytes() {
        if (error || no_tag || !complete)
            return null;

        byte bytes[] = baos.toByteArray();
        if (bytes.length < tagLength)
            return null;


        byte result[] = new byte[tagLength];
        System.arraycopy(bytes, 0, result, 0, tagLength);
        return result;
    }
}
