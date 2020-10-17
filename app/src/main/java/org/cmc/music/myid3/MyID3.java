/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 *
 */

package org.cmc.music.myid3;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * The primary interface to the MyID3 library.
 * <p>
 * Almost all of the MyID3 library's core functionality can be accessed through
 * it's methods.
 * <p>
 * See the source of the SampleUsage class and other classes in the
 * org.cmc.music.myid3.examples package for examples.
 *
 * @see org.cmc.music.myid3.examples.SampleUsage
 */
public class MyID3 {
    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p>
     *
     * @param file   File to read non-metadata (ie. song data) from. Will be
     *               overwritten with new mp3 file.
     * @param set    MusicMetadataSet, usually read from mp3 file.
     * @param values MusicMetadata, a specific group of values to write.
     * @see MusicMetadataSet, MusicMetadata
     */
    public void update(File file, MusicMetadataSet set, MusicMetadata values)
            throws UnsupportedEncodingException, IOException, ID3WriteException {
        File temp = null;
        temp = File.createTempFile(file.getName(), ".tmp", file
                .getParentFile());
        write(file, temp, set, values);
        temp.setLastModified(file.lastModified());
        file.delete();
        temp.renameTo(file);
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p>
     *
     * @param file     File to read non-metadata (ie. song data) from. Will be
     *                 overwritten with new mp3 file.
     * @param set      MusicMetadataSet, usually read from mp3 file.
     * @param values   MusicMetadata, a specific group of values to write.
     * @param filter   MyID3v2Write.Filter, can be used to prevent ID3v2 frames from
     *                 written on a case-by-case basis.
     * @param listener MyID3Listener, observer of the write process.
     * @see MusicMetadataSet, MusicMetadata, MyID3Listener, MyID3v2Write.Filter
     */
    public void update(File file, MusicMetadataSet set, MusicMetadata values,
                       MyID3v2Write.Filter filter, MyID3Listener listener)
            throws UnsupportedEncodingException, IOException, ID3WriteException {

        File temp = null;
        try {
            temp = File.createTempFile(file.getName(), ".tmp", file
                    .getParentFile());

            write(file, temp, set, values, filter, listener);
            temp.setLastModified(file.lastModified());
            file.delete();
            temp.renameTo(file);
        } catch (UnsupportedEncodingException e) {
            if (temp != null && temp.exists() && file.exists())
                temp.delete();
            throw e;
        } catch (IOException e) {
            if (temp != null && temp.exists() && file.exists())
                temp.delete();
            throw e;
        } catch (ID3WriteException e) {
            if (temp != null && temp.exists() && file.exists())
                temp.delete();
            throw e;
        }
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p>
     *
     * @param src    File to read non-metadata (ie. song data) from.
     * @param dst    File to overwrite with new mp3 file.
     * @param set    MusicMetadataSet, usually read from mp3 file.
     * @param values MusicMetadata, a specific group of values to write.
     * @see MusicMetadataSet, MusicMetadata
     */
    public void write(File src, File dst, MusicMetadataSet set,
                      MusicMetadata values) throws UnsupportedEncodingException,
            IOException, ID3WriteException {
        write(src, dst, set, values, null, null);
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p>
     *
     * @param src      File to read non-metadata (ie. song data) from.
     * @param dst      File to overwrite with new mp3 file.
     * @param set      MusicMetadataSet, usually read from mp3 file.
     * @param values   MusicMetadata, a specific group of values to write.
     * @param listener MyID3Listener, observer of the write process.
     * @see MusicMetadataSet, MusicMetadata, MyID3Listener
     */
    public void write(File src, File dst, MusicMetadataSet set,
                      MusicMetadata values, MyID3Listener listener)
            throws UnsupportedEncodingException, IOException, ID3WriteException {
        write(src, dst, set, values, null, listener);
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p>
     *
     * @param src      File to read non-metadata (ie. song data) from.
     * @param dst      File to overwrite with new mp3 file.
     * @param set      MusicMetadataSet, usually read from mp3 file.
     * @param values   MusicMetadata, a specific group of values to write.
     * @param filter   MyID3v2Write.Filter, can be used to prevent ID3v2 frames from
     *                 written on a case-by-case basis.
     * @param listener MyID3Listener, observer of the write process.
     * @see MusicMetadataSet, MusicMetadata, MyID3Listener, MyID3v2Write.Filter
     */
    public void write(File src, File dst, MusicMetadataSet set,
                      MusicMetadata values, MyID3v2Write.Filter filter,
                      MyID3Listener listener) throws UnsupportedEncodingException,
            IOException, ID3WriteException {
        if (values == null)


            if (listener != null)
                listener.log();

        byte id3v1Tag[] = new MyID3v1().toTag(values);
        if (listener != null)
            listener.log("writing id3v1Tag", id3v1Tag == null ? "null" : ""
                    + id3v1Tag.length);

        byte id3v2TailTag[] = new MyID3v2Write().toTag(filter, set, values);
        if (listener != null)
            listener.log("writing id3v2TailTag", id3v2TailTag == null ? "null"
                    : "" + id3v2TailTag.length);

        write(src, dst, id3v1Tag, id3v2TailTag, id3v2TailTag);

        if (listener != null)
            listener.log();
    }

    /**
     * Removes all ID3v1 and ID3v2 tags from an mp3 file.
     * <p>
     *
     * @param src File to read non-metadata (ie. song data) from.
     * @param dst File to overwrite with new mp3 file.
     */
    public void removeTags(File src, File dst)
            throws UnsupportedEncodingException, IOException, ID3WriteException {
        byte id3v1Tag[] = null;
        byte id3v2HeadTag[] = null;
        byte id3v2TailTag[] = null;

        write(src, dst, id3v1Tag, id3v2HeadTag, id3v2TailTag);
    }

    /**
     * Removes all ID3v1 and ID3v2 tags from an mp3 file.
     * <p>
     *
     * @param src File to read non-metadata (ie. song data) from.
     * @param dst File to overwrite with new mp3 file.
     */
    public void rewriteTags(File src, File dst)
            throws UnsupportedEncodingException, IOException, ID3WriteException {
        byte id3v1Tag[] = null;
        ID3Tag tag = readID3v1(src);
        if (null != tag)
            id3v1Tag = tag.bytes;

        byte id3v2HeadTag[] = readID3v2Head(src);

        boolean hasId3v1 = id3v1Tag != null;
        byte id3v2TailTag[] = readID3v2Tail(src, hasId3v1);

        write(src, dst, id3v1Tag, id3v2HeadTag, id3v2TailTag);
    }

    private boolean skipId3v1 = false;

    /**
     * Configures the library to not write ID3v1 tags.
     */
    public void setSkipId3v1() {
        skipId3v1 = true;
    }

    private boolean skipId3v2 = false;

    /**
     * Configures the library to not write ID3v2 tags.
     */
    public void setSkipId3v2() {
        skipId3v2 = true;
    }

    private boolean skipId3v2Head = false;

    /**
     * Configures the library to not write ID3v2 head tags.
     */
    public void setSkipId3v2Head() {
        skipId3v2Head = true;
    }

    private boolean skipId3v2Tail = false;

    /**
     * Configures the library to not write ID3v2 tail tags.
     */
    public void setSkipId3v2Tail() {
        skipId3v2Tail = true;
    }

    private void write(File src, File dst, byte id3v1Tag[],
                       byte id3v2HeadTag[], byte id3v2TailTag[]) throws IOException {
        if (src == null || !src.exists())


            if (!src.getName().toLowerCase().endsWith(".mp3"))


                if (dst == null)


                    if (dst.exists()) {
                        dst.delete();

                    }

        boolean hasId3v1 = hasID3v1(src);

        long id3v1Length = hasId3v1 ? 128 : 0;
        long id3v2HeadLength = findID3v2HeadLength(src);
        long id3v2TailLength = findID3v2TailLength(src, hasId3v1);

        OutputStream os = null;
        InputStream is = null;
        try {
            dst.getParentFile().mkdirs();
            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            if (!skipId3v2Head && !skipId3v2 && id3v2HeadTag != null)
                os.write(id3v2HeadTag);

            is = new FileInputStream(src);
            is = new BufferedInputStream(is, 8192);

            is.skip(id3v2HeadLength);

            long total_to_read = src.length();
            total_to_read -= id3v1Length;
            total_to_read -= id3v2HeadLength;
            total_to_read -= id3v2TailLength;

            byte buffer[] = new byte[1024];
            long total_read = 0;
            while (total_read < total_to_read) {
                int remainder = (int) (total_to_read - total_read);
                int readSize = Math.min(buffer.length, remainder);
                int read = is.read(buffer, 0, readSize);
                if (read <= 0)
                    throw new IOException("unexpected EOF");

                os.write(buffer, 0, read);
                total_read += read;
            }

            if (!skipId3v2Tail && !skipId3v2 && id3v2TailTag != null)
                os.write(id3v2TailTag);
            if (!skipId3v1 && id3v1Tag != null)
                os.write(id3v1Tag);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Throwable e) {

            }
            try {
                if (os != null)
                    os.close();
            } catch (Throwable e) {

            }
        }
    }

    private final byte[] readArray(InputStream is, int length)
            throws IOException {
        byte result[] = new byte[length];
        int total = 0;
        while (total < length) {
            int read = is.read(result, total, length - total);
            if (read < 0)
                throw new IOException("bad read");
            total += read;
        }
        return result;
    }

    /**
     * Reads all metadata (ID3v1 & ID3v2) from MP3 file.
     * <p>
     *
     * @param file File to read metadata (ie. song data) from.
     * @return MusicMetadataSet, a set of MusicMetadata value collections.
     * @see MusicMetadataSet, MusicMetadata
     */
    public MusicMetadataSet read(File file) throws IOException {
        return read(file, null);
    }

    /**
     * Reads all metadata (ID3v1 & ID3v2) from MP3 file.
     * <p>
     *
     * @param file     File to read metadata (ie. song data) from.
     * @param listener MyID3Listener, an observer.
     * @return MusicMetadataSet, a set of MusicMetadata value collections.
     * @see MusicMetadataSet, MusicMetadata
     */
    public MusicMetadataSet read(File file, MyID3Listener listener)
            throws IOException {
        try {
            if (file == null || !file.exists())
                return null;

            if (!file.getName().toLowerCase().endsWith(".mp3"))
                return null;

            ID3Tag id3v1 = readID3v1(listener, file);
            ID3Tag.V2 id3v2 = readID3v2(listener, file, id3v1 != null);

            return MusicMetadataSet.factoryMethod(id3v1,
                    id3v2, file.getName(), file.getParentFile().getName());
        } catch (Error e) {

            throw e;
        } catch (IOException e) {

            throw e;
        }
    }

    private ID3Tag readID3v1(File file) throws IOException {
        return readID3v1(null, file);
    }

    private ID3Tag readID3v1(MyID3Listener listener, File file)
            throws IOException {
        if (file == null || !file.exists())
            return null;

        long length = file.length();

        if (length < 128)
            return null;

        byte bytes[];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - 128);

            bytes = readArray(is, 128);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }

        if (bytes[0] != 'T')
            return null;
        if (bytes[1] != 'A')
            return null;
        if (bytes[2] != 'G')
            return null;

        if (null != listener)
            listener.log("ID3v1 tag found.");

        MyID3v1 id3v1 = new MyID3v1();
        MusicMetadata tags = id3v1.parseTags(listener, bytes);

        return new ID3Tag.V1(bytes, tags);
    }

    private boolean hasID3v1(File file) throws IOException {
        if (file == null || !file.exists())
            return false;

        long length = file.length();

        if (length < 128)
            return false;

        byte bytes[];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - 128);

            bytes = readArray(is, 128);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }

        if (bytes[0] != 'T')
            return false;
        if (bytes[1] != 'A')
            return false;
        if (bytes[2] != 'G')
            return false;

        return true;
    }

    private static final int ID3v2_HEADER_LENGTH = 10;

    private byte[] readID3v2Head(File file) throws IOException {

        if (file == null || !file.exists())
            return null;

        long length = file.length();

        if (length < ID3v2_HEADER_LENGTH)
            return null;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            byte header[];
            header = readArray(is, ID3v2_HEADER_LENGTH);

            if (header[0] != 0x49) // I
                return null;
            if (header[1] != 0x44) // D
                return null;
            if (header[2] != 0x33) // 3
                return null;

            int flags = header[5];
            boolean has_footer = (flags & (1 << 4)) > 0;

            Number tagLength = MyID3v2Read.readSynchsafeInt(header, 6);
            if (tagLength == null)
                return null;

            int bodyLength = tagLength.intValue();
            if (has_footer)
                bodyLength += ID3v2_HEADER_LENGTH;

            if (ID3v2_HEADER_LENGTH + bodyLength > length)
                return null;

            byte body[] = readArray(is, bodyLength);

            byte result[] = new byte[header.length + body.length];

            System.arraycopy(header, 0, result, 0, header.length);
            System.arraycopy(body, 0, result, header.length, body.length);

            return result;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }
    }

    private long findID3v2HeadLength(File file) throws IOException {
        if (file == null || !file.exists())
            return 0;

        long length = file.length();

        if (length < ID3v2_HEADER_LENGTH)
            return 0;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            byte header[];
            header = readArray(is, ID3v2_HEADER_LENGTH);

            if (header[0] != 0x49) // I
                return 0;
            if (header[1] != 0x44) // D
                return 0;
            if (header[2] != 0x33) // 3
                return 0;

            int flags = header[5];
            boolean has_footer = (flags & (1 << 4)) > 0;

            Number tagLength = MyID3v2Read.readSynchsafeInt(header, 6);
            if (tagLength == null)
                return 0;

            int totalLength = ID3v2_HEADER_LENGTH + tagLength.intValue();
            if (has_footer)
                totalLength += ID3v2_HEADER_LENGTH;

            return totalLength;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }
    }

    private int findID3v2TailLength(File file, boolean hasId3v1)
            throws IOException {
        if (file == null || !file.exists())
            return 0;

        long length = file.length();

        int index = hasId3v1 ? 128 : 0;
        index += ID3v2_HEADER_LENGTH;

        if (index > length)
            return 0;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - index);

            byte footer[];
            footer = readArray(is, ID3v2_HEADER_LENGTH);

            if (footer[0] != 0x33) // 3
                return 0;
            if (footer[1] != 0x44) // D
                return 0;
            if (footer[2] != 0x49) // I
                return 0;

            Number tagLength = MyID3v2Read.readSynchsafeInt(footer, 6);
            if (tagLength == null)
                return 0;

            int totalLength = ID3v2_HEADER_LENGTH + ID3v2_HEADER_LENGTH
                    + tagLength.intValue();

            return totalLength;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }
    }

    private byte[] readID3v2Tail(File file, boolean hasId3v1)
            throws IOException {
        if (file == null || !file.exists())
            return null;

        long length = file.length();

        int index = hasId3v1 ? 128 : 0;
        index += ID3v2_HEADER_LENGTH;

        if (index > length)
            return null;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - index);

            byte footer[];
            footer = readArray(is, ID3v2_HEADER_LENGTH);

            if (footer[2] != 0x33) // 3
                return null;
            if (footer[1] != 0x44) // D
                return null;
            if (footer[0] != 0x49) // I
                return null;

            Number tagLength = MyID3v2Read.readSynchsafeInt(footer, 6);
            if (tagLength == null)
                return null;

            int bodyLength = tagLength.intValue();
            if (index + bodyLength > length)
                return null;

            is.close();
            is = null;

            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            long skip = length;
            skip -= ID3v2_HEADER_LENGTH;
            skip -= bodyLength;
            skip -= ID3v2_HEADER_LENGTH;
            if (hasId3v1)
                skip -= 128;
            is.skip(skip);

            byte header_and_body[] = readArray(is, ID3v2_HEADER_LENGTH
                    + bodyLength + ID3v2_HEADER_LENGTH);

            byte result[] = header_and_body;

            return result;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
        }
    }

    private ID3Tag.V2 readID3v2(MyID3Listener listener, File file,
                                boolean hasId3v1) throws IOException {
        if (file == null || !file.exists())
            return null;

        byte bytes[] = null;
        bytes = readID3v2Tail(file, hasId3v1);
        if (bytes == null)
            bytes = readID3v2Head(file);

        if (bytes == null)
            return null;

        if (null != listener)
            listener.log("ID3v2 tag found: " + bytes.length + " bytes");

        MyID3v2Read parser = new MyID3v2Read(listener,
                new ByteArrayInputStream(bytes), false);
        while (!parser.isComplete()) {
            parser.iteration();
        }
        if (parser.isError()) {
            if (listener != null)
                listener.log("id3v2 error", parser.getErrorMessage());

            parser.dump();
            return null;
        }

        if (!parser.hasTags())
            return null;

        Vector tags = parser.getTags();

        MusicMetadata values = new ID3v2DataMapping().process(tags);

        byte version_major = parser.getVersionMajor();
        byte version_minor = parser.getVersionMinor();

        if (null != listener)
            listener.log();

        return new ID3Tag.V2(version_major, version_minor, bytes, values, tags);
    }

}
