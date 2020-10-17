package ru.euphoria.doggy.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides static utils methods for manipulation with the {@link java.io.InputStream}.
 * and {@link java.io.OutputStream} packages.
 * <p/>
 * e.g. read, write, copy, ....
 *
 * @author Igor Morozkin
 * @since 2.0
 */
public class Streams {
    /**
     * The default buffer size, 8K bytes
     */
    public static final int BUFFER_SIZE = 8192;

    /**
     * The default char buffer size, 4K chars (8K bytes)
     */
    public static final int CHAR_BUFFER_SIZE = 4096;

    // only static methods
    private Streams() {
    }

    /**
     * Reads all characters from specified {@link InputStream},
     * with using UTF-8 charset.
     *
     * @param from the input stream object to read from
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public static String read(InputStream from) throws IOException {
        return read(from, UTF_8);
    }

    /**
     * Reads all characters from specified {@link InputStream},
     * with using UTF-8 charset.
     *
     * @param from     the input stream object to read from
     * @param encoding the encoding to convert byte into char
     * @throws IOException if an I/O error occurs reading from the source
     */
    public static String read(InputStream from, Charset encoding) throws IOException {
        return read(new InputStreamReader(from, encoding));
    }

    /**
     * Reads all characters from specified {@link Reader}
     *
     * @param from the reader object to read from
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public static String read(Reader from) throws IOException {
        StringWriter builder = new StringWriter(CHAR_BUFFER_SIZE);
        try {
            copy(from, builder);
            return builder.toString();
        } finally {
            close(from);
        }
    }

    /**
     * Read all bytes from specified {@link InputStream} into a byte array.
     *
     * @param from the input stream object to read from
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public static byte[] readBytes(InputStream from) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.max(from.available(), BUFFER_SIZE));
        try {
            copy(from, output);
        } finally {
            close(from);
        }
        return output.toByteArray();
    }

    /**
     * Writes byte array into specified output stream.
     *
     * @param from the bytes to write
     * @param to   the stream to write into
     * @throws IOException if an I/O error occurs writing to file
     */
    public static void write(byte[] from, OutputStream to) throws IOException {
        try {
            to.write(from);
            to.flush();
        } finally {
            close(to);
        }
    }

    /**
     * Writes text data into specified output stream.
     *
     * @param from the text data to write
     * @param to   the stream to write into
     * @throws IOException if an I/O error occurs writing to file
     */
    public static void write(String from, OutputStream to) throws IOException {
        write(from, new OutputStreamWriter(to, UTF_8));
    }

    /**
     * Writes all characters into specified writer.
     *
     * @param from the char array to write
     * @param to   the writer to write into
     * @throws IOException if an I/O error occurs writing to file
     */
    public static void write(char[] from, Writer to) throws IOException {
        try {
            to.write(from);
            to.flush();
        } finally {
            close(to);
        }
    }

    /**
     * Writes text data into specified writer.
     *
     * @param from the text data to write
     * @param to   the writer to write into
     * @throws IOException if an I/O error occurs writing to file
     */
    public static void write(String from, Writer to) throws IOException {
        try {
            to.write(from);
            to.flush();
        } finally {
            close(to);
        }
    }

    /**
     * Copies all characters from the {@link Reader} to {@link Writer} objects.
     * Don't close or flush either object.
     *
     * @param from the object to read from
     * @param to   the object to write to
     * @return the number of characters copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(Reader from, Writer to) throws IOException {
        char[] buffer = new char[CHAR_BUFFER_SIZE];
        int read;
        long total = 0;

        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
            total += read;
        }
        return total;
    }

    /**
     * Copies all bytes from the {@link InputStream} to {@link OutputStream} objects.
     * Don't close or flush either object.
     *
     * @param from the object to read from
     * @param to   the object to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        long total = 0;

        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
            total += read;
        }
        return total;
    }

    /**
     * Returns wrapped specified {@link InputStream} into in-memory buffer of 8K bytes.
     * Use this method for effect read.
     *
     * @param input the stream to wrap into buffer
     */
    public static BufferedInputStream buffer(InputStream input) {
        return buffer(input, BUFFER_SIZE);
    }

    /**
     * Returns wrapped specified {@link InputStream} into in-memory buffer.
     * Use this method for effect read.
     *
     * @param input the stream to wrap into buffer
     * @param size  the buffer size in bytes
     */
    public static BufferedInputStream buffer(InputStream input, int size) {
        return input instanceof BufferedInputStream ? (BufferedInputStream) input
                : new BufferedInputStream(input, size);
    }

    /**
     * Returns wrapped specified {@link OutputStream} into in-memory buffer of 8K bytes.
     * Use this method for effect write.
     *
     * @param output the stream to wrap into buffer
     */
    public static BufferedOutputStream buffer(OutputStream output) {
        return buffer(output, BUFFER_SIZE);
    }

    /**
     * Returns wrapped specified {@link OutputStream} into in-memory buffer.
     * Use this method for effect write.
     *
     * @param output the stream to wrap into buffer
     * @param size   the buffer size in bytes
     */
    public static BufferedOutputStream buffer(OutputStream output, int size) {
        return output instanceof BufferedOutputStream ? (BufferedOutputStream) output
                : new BufferedOutputStream(output, size);
    }

    /**
     * Returns wrapped specified {@link Reader} into in-memory buffer of 4K chars.
     * Use this method for effect read.
     *
     * @param input the stream to wrap into buffer
     */
    public static BufferedReader buffer(Reader input) {
        return buffer(input, CHAR_BUFFER_SIZE);
    }

    /**
     * Returns wrapped specified {@link Reader} into in-memory buffer.
     * Use this method for effect read.
     *
     * @param input the stream to wrap into buffer
     * @param size  the buffer size in bytes
     */
    public static BufferedReader buffer(Reader input, int size) {
        return input instanceof BufferedReader ? (BufferedReader) input
                : new BufferedReader(input, size);
    }

    /**
     * Returns wrapped specified {@link Writer} into in-memory buffer of 4K chars.
     * Use this method for effect write.
     *
     * @param output the stream to wrap into buffer
     */
    public static BufferedWriter buffer(Writer output) {
        return buffer(output, CHAR_BUFFER_SIZE);
    }

    /**
     * Returns wrapped specified {@link Writer} into in-memory buffer.
     * Use this method for effect write.
     *
     * @param output the stream to wrap into buffer
     * @param size   the buffer size in bytes
     */
    public static BufferedWriter buffer(Writer output, int size) {
        return output instanceof BufferedWriter ? (BufferedWriter) output
                : new BufferedWriter(output, size);
    }

    /**
     * Returns wrapped specified {@link InputStream} into {@link GZIPInputStream}.
     * Use this method to read from GZIP data.
     *
     * @param input the stream to wrap into gzip
     */
    public static GZIPInputStream gzip(InputStream input) throws IOException {
        return gzip(input, BUFFER_SIZE);
    }

    /**
     * Returns wrapped specified {@link InputStream} into {@link GZIPInputStream}.
     * Use this method to read from GZIP data.
     *
     * @param input the stream to wrap into gzip
     * @param size  the buffer size in bytes
     */
    public static GZIPInputStream gzip(InputStream input, int size) throws IOException {
        return input instanceof GZIPInputStream ? (GZIPInputStream) input
                : new GZIPInputStream(input, size);
    }

    /**
     * Returns wrapped specified {@link OutputStream} into {@link GZIPOutputStream}.
     * Use this method to write data in GZIP format.
     *
     * @param input the stream to wrap into gzip
     */
    public static GZIPOutputStream gzip(OutputStream input) throws IOException {
        return gzip(input, BUFFER_SIZE);
    }

    /**
     * Returns wrapped specified {@link OutputStream} into {@link GZIPOutputStream}.
     * Use this method to write data in GZIP format.
     *
     * @param input the stream to wrap into gzip
     * @param size  the buffer size in bytes
     */
    public static GZIPOutputStream gzip(OutputStream input, int size) throws IOException {
        return input instanceof GZIPOutputStream ? (GZIPOutputStream) input
                : new GZIPOutputStream(input, size);
    }

    /**
     * Closes the specified {@link Closeable} object without throws {@link Exception}.
     *
     * @param c the object to close, may me null or already closed
     * @return true if object is closed, false otherwise
     */
    public static boolean close(Closeable c) {
        if (c != null) {
            try {
                c.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
