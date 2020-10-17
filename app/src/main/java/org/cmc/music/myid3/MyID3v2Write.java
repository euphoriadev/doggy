package org.cmc.music.myid3;

import org.cmc.music.common.ID3FrameType;
import org.cmc.music.common.ID3WriteException;
import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class MyID3v2Write implements MyID3v2Constants {

    private final int id3v2_version = 3;

    private byte[] getHeaderFooter(int body_length, boolean is_footer)
            throws ID3WriteException {
        byte result[] = new byte[10];

        int index = 0;
        if (is_footer) {
            result[index++] = 0x33; // 3
            result[index++] = 0x44; // D
            result[index++] = 0x49; // I
        } else {
            result[index++] = 0x49; // I
            result[index++] = 0x44; // D
            result[index++] = 0x33; // 3
        }

        if (id3v2_version == 4)
            result[index++] = 0x04; // version
        else if (id3v2_version == 3)
            result[index++] = 0x03; // version
        else
            throw new ID3WriteException("id3v2_version: " + id3v2_version);

        result[index++] = 0x00;

        int flags = 0; // charles
        if (id3v2_version == 4)
            flags |= HEADER_FLAG_ID3v24_FOOTER_PRESENT;
        else if (id3v2_version == 3) {
        } else
            throw new ID3WriteException("id3v2_version: " + id3v2_version);

        result[index++] = (byte) flags;

        writeSynchSafeInt(result, index, body_length);

        return result;
    }

    private final void writeSynchSafeInt(byte bytes[], int start, int value)
            throws ID3WriteException {
        bytes[start + 3] = (byte) (value & 0x7f);
        value >>= 7;
        bytes[start + 2] = (byte) (value & 0x7f);
        value >>= 7;
        bytes[start + 1] = (byte) (value & 0x7f);
        value >>= 7;
        bytes[start] = (byte) (value & 0x7f);

        value >>= 7;
        if (value != 0)
            throw new ID3WriteException("Value to large for synch safe int: "
                    + value);
    }

    private static class Frame {
        public final String longFrameID;
        public final Number frame_order;
        public final byte bytes[];
        public final ID3v2FrameFlags flags;

        public Frame(String longFrameID, byte[] bytes) {
            this(longFrameID, ID3FrameType.DEFAULT_FRAME_ORDER, bytes,
                    new ID3v2FrameFlags());
        }

        public Frame(String longFrameID, byte[] bytes,
                     final ID3v2FrameFlags flags) {
            this(longFrameID, ID3FrameType.DEFAULT_FRAME_ORDER, bytes, flags);
        }

        public Frame(String longFrameID, Number frame_order, byte[] bytes) {
            this(longFrameID, frame_order, bytes, new ID3v2FrameFlags());
        }

        public Frame(String longFrameID, Number frame_order, byte[] bytes,
                     final ID3v2FrameFlags flags) {
            this.longFrameID = longFrameID;
            this.frame_order = frame_order;
            // this.frame_type = frame_type;
            this.bytes = bytes;
            this.flags = flags;
        }

        public String toString() {
            return "[frame: " + longFrameID + ": " + bytes.length + "]";
        }
    }

    private static final ID3v2DataMapping mapping = new ID3v2DataMapping();

    private Frame toFrameKey(Object key, Object value)
            throws IOException {
        return toFrameKey(key, value, null);
    }

    private Frame toFrameKey(Object key, Object value1, Object value2)
            throws IOException {
        ID3FrameType frame_type = mapping.getID3FrameType(key);


        if (frame_type == null) {
            return null;
        }

        return toFrame(frame_type, value1, value2);
    }

    private Frame toFrame(ID3FrameType frame_type, Object value1)
            throws IOException {
        return toFrame(frame_type.long_id, frame_type.getFrameOrder(), value1,
                null);
    }

    private Frame toFrame(ID3FrameType frame_type, Object value1, Object value2)
            throws IOException {
        return toFrame(frame_type.long_id, frame_type.getFrameOrder(), value1,
                value2);
    }

    private Frame toFrame(String longFrameID, Number frame_order,
                          Object value1, Object value2) throws
            IOException {
        if (longFrameID.startsWith("T")) {
            return toFrameText(longFrameID, frame_order, value1, value2);
        } else if (longFrameID.equals("COMM")) {
            return toFrameCOMM(longFrameID, frame_order, value1);
        } else {
            // TODO: should we throw an exception here?


            return null;
        }
    }

    private boolean canEncodeStringInISO(String s)
            throws UnsupportedEncodingException {
        byte bytes[] = s.getBytes(CHAR_ENCODING_ISO);
        String check1 = new String(bytes, CHAR_ENCODING_ISO);

        return check1.equals(s);
    }

    private byte[] encodeString(String s, boolean use_iso)
            throws IOException {
        if (use_iso)
            return s.getBytes(CHAR_ENCODING_ISO);
        else {
            byte bytes[] = s.getBytes(CHAR_ENCODING_UTF_16);

            // Windows Media Player can't handle UTF-16, big-endian.
            // switch to UTF-16, little-endian.
            if (((0xff & bytes[0]) == 0xFE) && ((0xff & bytes[1]) == 0xFF)) {
                // manually switch UTF 16 byte order
                for (int i = 0; i < bytes.length; i += 2) {
                    byte temp = bytes[i];
                    bytes[i] = bytes[i + 1];
                    bytes[i + 1] = temp;
                }
            }
            return bytes;
        }
    }

    private Frame toFrameText(String longFrameID, Number frame_order,
                              Object value1, Object value2) throws
            IOException {
        String s1;
        if (value1 instanceof String)
            s1 = (String) value1;
        else if (value1 instanceof Number)
            s1 = value1.toString();
        else {


            return null;
        }
        String s2 = null;
        if (value2 instanceof String)
            s2 = (String) value2;
        else if (value2 instanceof Number)
            s2 = value2.toString();

        boolean use_iso = canEncodeStringInISO(s1);
        if (s2 != null)
            use_iso &= canEncodeStringInISO(s2);

        int char_encoding_code = use_iso ? CHAR_ENCODING_CODE_ISO_8859_1
                : CHAR_ENCODING_CODE_UTF_16_WITH_BOM;
        // : CHAR_ENCODING_CODE_UTF_8;

        byte string_1_bytes[] = encodeString(s1, use_iso);
        byte string_2_bytes[] = null;
        if (s2 != null)
            string_2_bytes = encodeString(s2, use_iso);


        // int frame_length = kFRAME_HEADER_LENGTH + string_bytes.length + 1;
        int result_length = string_1_bytes.length + 1;
        if (string_2_bytes != null)
            result_length += string_2_bytes.length + 1;
        byte result[] = new byte[result_length];
        int index = 0;

        result[index++] = (byte) char_encoding_code;
        System.arraycopy(string_1_bytes, 0, result, index,
                string_1_bytes.length);
        index += string_1_bytes.length;

        if (string_2_bytes != null) {
            result[index++] = (byte) char_encoding_code;
            System.arraycopy(string_2_bytes, 0, result, index,
                    string_2_bytes.length);
        }

        return new Frame(longFrameID, frame_order, result);
    }

    private Frame toFrameImage(String longFrameID, Number frame_order,
                               ImageData imageData)
        // byte imageData[], String mimeType, String description,
        // int pictureType)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        boolean use_iso = canEncodeStringInISO(imageData.description);

        int char_encoding_code = use_iso ? CHAR_ENCODING_CODE_ISO_8859_1
                : CHAR_ENCODING_CODE_UTF_16_WITH_BOM;

        baos.write(char_encoding_code);

        byte mimeTypeBytes[] = encodeString(imageData.mimeType, true);


        baos.write(mimeTypeBytes);
        baos.write(0);

        baos.write(0xff & imageData.pictureType);

        byte descriptionBytes[] = encodeString(imageData.description, use_iso);
        baos.write(descriptionBytes);


        baos.write(0);

        baos.write(imageData.imageData);

        byte frameBytes[] = baos.toByteArray();


        return new Frame(longFrameID, frame_order, frameBytes);
    }

    private Frame toFrameCOMM(String longFrameID, Number frame_order,
                              Object value) throws IOException {
        String s;
        if (value instanceof String)
            s = (String) value;
        else {
            return null;
        }

        boolean use_iso = canEncodeStringInISO(s);

        int char_encoding_code = use_iso ? CHAR_ENCODING_CODE_ISO_8859_1
                : CHAR_ENCODING_CODE_UTF_16_WITH_BOM;
        // : CHAR_ENCODING_CODE_UTF_8;

        byte string_bytes[] = encodeString(s, use_iso);

        // int frame_length = kFRAME_HEADER_LENGTH + string_bytes.length + 1;
        int result_length = string_bytes.length + 1 + 3 + 1;

        byte result[] = new byte[result_length];
        int index = 0;

        result[index++] = (byte) char_encoding_code;
        result[index++] = (byte) 0; // language
        result[index++] = (byte) 0; // language
        result[index++] = (byte) 0; // language

        // summary
        result[index++] = (byte) 0; // divider

        System.arraycopy(string_bytes, 0, result, index, string_bytes.length);

        return new Frame(longFrameID, frame_order, result);
    }

    private Vector toFrames(MusicMetadata values)
            throws IOException {
        Vector result = new Vector();

        {
            Object track_count = values.get(KEY_TRACK_COUNT);
            Object track_number = values.get(KEY_TRACK_NUMBER);

            if (track_count != null || track_number != null) {
                String value = "";
                if (track_number != null)
                    value += track_number.toString();
                if (track_count != null) {
                    value += "/";
                    value += track_count.toString();
                }

                result.add(toFrame(ID3FrameType.TRACKNUM, value));
            }

            values.remove(KEY_TRACK_COUNT); // charles
            values.remove(KEY_TRACK_NUMBER); // charles
        }
        {
            String genreString = "";
            String genreIDString = null;
            Object value = values.get(KEY_GENRE);
            if (value != null) {
                genreString = value.toString();
                Number id = ID3v1Genre.get(genreString);
                if (null != id) {
                    genreIDString = id.toString();
                    genreString = "(" + id + ")" + genreString;
                }
            } else {
                value = values.get(KEY_GENRE_ID);
                if (value != null) {
                    if (genreIDString == null
                            || !genreIDString.equals(value.toString()))
                        genreString = "(" + genreIDString + ")" + genreString;
                }
            }

            if (genreString.length() > 0)
                result.add(toFrame(ID3FrameType.CONTENTTYPE, genreString));

            values.remove(KEY_GENRE); // charles
            values.remove(KEY_GENRE_ID); // charles
        }
        {
            Object value = values.get(KEY_DURATION_SECONDS);


            if (value != null) {
                Number number = (Number) value;
                number = (long) (number.intValue() * 1000);
                result.add(toFrame(ID3FrameType.SONGLEN, number.toString()));
            }

            values.remove(KEY_DURATION_SECONDS); // charles
        }

        {
            Object value = values.get(KEY_COMMENT);

            if (value != null) {
                Frame frame = toFrame(ID3FrameType.COMMENT, value.toString());

                if (frame != null)
                    result.add(frame);
                else {


                }

            }

            values.remove(KEY_COMMENT); // charles
        }

        Vector keys = new Vector(values.keySet());
        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            Object value = values.get(key);

            if (key.equals(KEY_PICTURES)) {
                Vector images = (Vector) value;
                for (int j = 0; j < images.size(); j++) {
                    ImageData imageData = (ImageData) images.get(j);
                    String longFrameID = ID3FrameType.PICTURE.long_id;
                    Number frame_order = ID3FrameType.PICTURE.getFrameOrder();
                    Frame frame = toFrameImage(longFrameID, frame_order,
                            imageData);
                    result.add(frame);
                }
                // return ID3FrameType.PICTURE;
                continue;
            }

            Frame frame = toFrameKey(key, value);
            if (frame != null)
                result.add(frame);
            else {


            }
        }

        return result;
    }

    private static final Comparator FRAME_SORTER = (o1, o2) -> {
        Frame f1 = (Frame) o1;
        Frame f2 = (Frame) o2;

        int fo1 = f1.frame_order.intValue();
        int fo2 = f2.frame_order.intValue();
        if (fo1 != fo2)
            return fo1 - fo2;

        return f1.longFrameID.compareTo(f2.longFrameID);
    };

    public interface Filter {
        boolean filter(String frameid);
    }

    private byte[] writeFrames(Filter filter, Vector v)
            throws ID3WriteException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Collections.sort(v, FRAME_SORTER);

        for (int i = 0; i < v.size(); i++) {
            Frame frame = (Frame) v.get(i);

            String frame_id = frame.longFrameID;
            if (frame_id.length() != 4)
                throw new ID3WriteException("frame_id has bad length: "
                        + frame_id + " (" + frame_id.length() + ")");

            if (filter != null && filter.filter(frame_id)) {
                continue;
            }

            // baos.write(frame_id );
            baos.write((byte) frame_id.charAt(0));
            baos.write((byte) frame_id.charAt(1));
            baos.write((byte) frame_id.charAt(2));
            baos.write((byte) frame_id.charAt(3));

            int length = frame.bytes.length;

            if (id3v2_version == 4) {
                baos.write((byte) (0x7f & (length >> 21)));
                baos.write((byte) (0x7f & (length >> 14)));
                baos.write((byte) (0x7f & (length >> 7)));
                baos.write((byte) (0x7f & (length)));
            } else if (id3v2_version == 3) {
                baos.write((byte) (0xff & (length >> 24)));
                baos.write((byte) (0xff & (length >> 16)));
                baos.write((byte) (0xff & (length >> 8)));
                baos.write((byte) (0xff & (length)));
            } else
                throw new ID3WriteException("id3v2_version: " + id3v2_version);

            // int flags = frame.flags;
            int flags = 0;
            if (id3v2_version == 4) {
                if (frame.flags.getTagAlterPreservation())
                    flags |= FRAME_FLAG_ID3v24_TAG_ALTER_PRESERVATION;
                if (frame.flags.getFileAlterPreservation())
                    flags |= FRAME_FLAG_ID3v24_FILE_ALTER_PRESERVATION;
                if (frame.flags.getReadOnly())
                    flags |= FRAME_FLAG_ID3v24_READ_ONLY;
                if (frame.flags.getGroupingIdentity())
                    flags |= FRAME_FLAG_ID3v24_GROUPING_IDENTITY;
                if (frame.flags.getCompression())
                    flags |= FRAME_FLAG_ID3v24_COMPRESSION;
                if (frame.flags.getEncryption())
                    flags |= FRAME_FLAG_ID3v24_ENCRYPTION;
                if (frame.flags.getUnsynchronisation())
                    flags |= FRAME_FLAG_ID3v24_UNSYNCHRONISATION;
                if (frame.flags.getDataLengthIndicator())
                    flags |= FRAME_FLAG_ID3v24_DATA_LENGTH_INDICATOR;
            } else if (id3v2_version == 3) {
                if (frame.flags.getTagAlterPreservation())
                    flags |= FRAME_FLAG_ID3v23_TAG_ALTER_PRESERVATION;
                if (frame.flags.getFileAlterPreservation())
                    flags |= FRAME_FLAG_ID3v23_FILE_ALTER_PRESERVATION;
                if (frame.flags.getReadOnly())
                    flags |= FRAME_FLAG_ID3v23_READ_ONLY;
                if (frame.flags.getGroupingIdentity())
                    flags |= FRAME_FLAG_ID3v23_GROUPING_IDENTITY;
                if (frame.flags.getCompression())
                    flags |= FRAME_FLAG_ID3v23_COMPRESSION;
                if (frame.flags.getEncryption())
                    flags |= FRAME_FLAG_ID3v23_ENCRYPTION;
            } else
                throw new ID3WriteException("id3v2_version: " + id3v2_version);

            baos.write((byte) (0xff & (flags >> 8)));
            baos.write((byte) (0xff & (flags)));

            baos.write(frame.bytes);
        }

        return baos.toByteArray();
    }

    private void checkTags(MusicMetadataSet set, Vector frames)
            throws IOException {
        if (set.id3v2Raw == null)
            return;

        Vector old_frames = set.id3v2Raw.frames;
        if (old_frames == null)
            return;

        Vector new_frame_ids = new Vector();
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = (Frame) frames.get(i);

            new_frame_ids.add(frame.longFrameID);
        }

        Vector final_frame_ids = new Vector(new_frame_ids);
        for (int i = 0; i < old_frames.size(); i++) {
            MyID3v2Frame old_frame = (MyID3v2Frame) old_frames.get(i);

            String longFrameID;
            Number frame_order;
            {
                ID3FrameType frame_type = ID3FrameType.get(old_frame.frame_id);
                if (frame_type != null) {
                    longFrameID = frame_type.long_id;
                    frame_order = frame_type.getFrameOrder();
                } else if (old_frame.frame_id.length() == 4) {
                    longFrameID = old_frame.frame_id;
                    frame_order = ID3FrameType.DEFAULT_FRAME_ORDER;
                } else {


                    continue;
                }
            }

            if (new_frame_ids.contains(longFrameID))
                continue;


            if (old_frame instanceof MyID3v2FrameText) {
                MyID3v2FrameText text_frame = (MyID3v2FrameText) old_frame;

                // ID3FrameType frame_type =
                // ID3FrameType.get(old_frame.frame_id);


                Frame frame = toFrame(longFrameID, frame_order,
                        text_frame.value, text_frame.value2);

                if (frame != null) {
                    frames.add(frame);
                    final_frame_ids.add(frame.longFrameID);
                } else {


                }
            } else if (old_frame instanceof MyID3v2FrameImage) {
                MyID3v2FrameImage imageFrame = (MyID3v2FrameImage) old_frame;


                Frame frame = toFrameImage(longFrameID, frame_order, imageFrame
                        .getImageData());

                frames.add(frame);
                final_frame_ids.add(frame.longFrameID);
            } else {
                MyID3v2FrameData data = (MyID3v2FrameData) old_frame;
                if (data.flags.getTagAlterPreservation())
                    continue;
                // if(data.flags.getTagAlterPreservation())
                // continue;
                if (data.frame_id.length() == 4) {
                    // if(data.flags.getCompression() ||
                    // data.flags.getUnsynchronisation() || )
                    // int flags = data.flags.flags;
                    Frame frame = new Frame(data.frame_id, data.data_bytes,
                            data.flags);
                    frames.add(frame);
                    final_frame_ids.add(frame.longFrameID);
                    continue;
                }


            }
            // if()
        }

    }

    public byte[] toTag(MusicMetadataSet set, MusicMetadata values)
            throws Exception {
        return toTag(null, set, values);
    }

    public byte[] toTag(Filter filter, MusicMetadataSet set,
                        MusicMetadata values) throws
            IOException, ID3WriteException {
        values = new MusicMetadata(values);


        Vector frames = toFrames(values);


        checkTags(set, frames);


        byte frame_bytes[] = writeFrames(filter, frames);


        byte extended_header[] = {};
        byte padding[] = {};

        int body_length = extended_header.length + frame_bytes.length
                + padding.length;

        byte header[] = getHeaderFooter(body_length, false);


        byte footer[];
        if (id3v2_version == 4)
            footer = getHeaderFooter(body_length, true);
        else if (id3v2_version == 3)
            footer = null;
        else
            throw new ID3WriteException("id3v2_version: " + id3v2_version);


        int resultLength = header.length + extended_header.length
                + frame_bytes.length + padding.length;
        if (footer != null)
            resultLength += footer.length;
        byte result[] = new byte[resultLength];

        int index = 0;
        System.arraycopy(header, 0, result, index, header.length);
        index += header.length;
        System.arraycopy(frame_bytes, 0, result, index, frame_bytes.length);
        if (footer != null) {
            index += frame_bytes.length;
            System.arraycopy(footer, 0, result, index, footer.length);
        }
        // index += footer.length;

        return result;
    }
}
