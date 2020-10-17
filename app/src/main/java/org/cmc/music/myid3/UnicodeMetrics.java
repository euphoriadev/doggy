package org.cmc.music.myid3;

import java.io.IOException;

public abstract class UnicodeMetrics implements MyID3v2Constants {
    //	public final int findEnd(byte bytes[]) throws IOException
    //	{
    //		return findEnd(bytes, 0);
    //	}

    public final int findEndWithTerminator(byte bytes[], int index)
            throws IOException {
        return findEnd(bytes, index, true);
    }

    public final int findEndWithoutTerminator(byte bytes[], int index)
            throws IOException {
        return findEnd(bytes, index, false);
    }

    protected abstract int findEnd(byte bytes[], int index,
                                   boolean includeTerminator) throws IOException;

    public static UnicodeMetrics getInstance(int charEncodingCode)
            throws IOException {
        switch (charEncodingCode) {
            case CHAR_ENCODING_CODE_ISO_8859_1:
                return new UnicodeMetricsASCII();
            case CHAR_ENCODING_CODE_UTF_8:

                return new UnicodeMetricsUTF8();
            case CHAR_ENCODING_CODE_UTF_16_WITH_BOM:

                return new UnicodeMetricsUTF16WithBOM();
            case CHAR_ENCODING_CODE_UTF_16_NO_BOM:

                return new UnicodeMetricsUTF16NoBOM();
            default:
                throw new IOException("Unknown char encoding code: "
                        + charEncodingCode);
        }
    }

    private static class UnicodeMetricsASCII extends UnicodeMetrics {
        public int findEnd(byte bytes[], int index, boolean includeTerminator)
                throws IOException {
            for (int i = index; i < bytes.length; i++) {
                if (bytes[i] == 0)
                    return includeTerminator ? i + 1 : i;
            }
            return bytes.length;
            //			throw new IOException("Terminator not found.");
        }
    }

    private static class UnicodeMetricsUTF8 extends UnicodeMetrics {

        public int findEnd(byte bytes[], int index, boolean includeTerminator)
                throws IOException {
            // http://en.wikipedia.org/wiki/UTF-8

            while (true) {
                if (index == bytes.length)
                    return bytes.length;
                if (index > bytes.length)
                    throw new IOException("Terminator not found.");

                int c1 = 0xff & bytes[index++];
                if (c1 == 0)
                    return includeTerminator ? index : index - 1;
                else if (c1 <= 0x7f)
                    continue;
                else if (c1 <= 0xDF) {
                    if (index >= bytes.length)
                        throw new IOException("Invalid unicode.");

                    int c2 = 0xff & bytes[index++];
                    if (c2 < 0x80 || c2 > 0xBF)
                        throw new IOException("Invalid code point.");
                } else if (c1 <= 0xEF) {
                    if (index >= bytes.length - 1)
                        throw new IOException("Invalid unicode.");

                    int c2 = 0xff & bytes[index++];
                    if (c2 < 0x80 || c2 > 0xBF)
                        throw new IOException("Invalid code point.");
                    int c3 = 0xff & bytes[index++];
                    if (c3 < 0x80 || c3 > 0xBF)
                        throw new IOException("Invalid code point.");
                } else if (c1 <= 0xF4) {
                    if (index >= bytes.length - 2)
                        throw new IOException("Invalid unicode.");

                    int c2 = 0xff & bytes[index++];
                    if (c2 < 0x80 || c2 > 0xBF)
                        throw new IOException("Invalid code point.");
                    int c3 = 0xff & bytes[index++];
                    if (c3 < 0x80 || c3 > 0xBF)
                        throw new IOException("Invalid code point.");
                    int c4 = 0xff & bytes[index++];
                    if (c4 < 0x80 || c4 > 0xBF)
                        throw new IOException("Invalid code point.");
                } else
                    throw new IOException("Invalid code point.");
            }
        }
    }

    private abstract static class UnicodeMetricsUTF16 extends UnicodeMetrics {
        protected static final int BYTE_ORDER_BIG_ENDIAN = 0;
        protected static final int BYTE_ORDER_LITTLE_ENDIAN = 1;
        protected int byteOrder = BYTE_ORDER_BIG_ENDIAN;

        public UnicodeMetricsUTF16(int byteOrder) {
            this.byteOrder = byteOrder;
        }

        public int findEnd(byte bytes[], int index, boolean includeTerminator)
                throws IOException {
            // http://en.wikipedia.org/wiki/UTF-16/UCS-2

            while (true) {
                if (index == bytes.length)
                    return bytes.length;
                if (index > bytes.length - 1)
                    throw new IOException("Terminator not found.");

                int c1 = 0xff & bytes[index++];
                int c2 = 0xff & bytes[index++];
                int msb1 = byteOrder == BYTE_ORDER_BIG_ENDIAN ? c1 : c2;

                if (c1 == 0 && c2 == 0) {
                    return includeTerminator ? index : index - 2;
                } else if (msb1 >= 0xD8) {
                    if (index > bytes.length - 1)
                        throw new IOException("Terminator not found.");

                    // second word.
                    int c3 = 0xff & bytes[index++];
                    int c4 = 0xff & bytes[index++];
                    int msb2 = byteOrder == BYTE_ORDER_BIG_ENDIAN ? c3 : c4;
                    if (msb2 < 0xDC)
                        throw new IOException("Invalid code point.");
                }
            }
        }
    }

    private static class UnicodeMetricsUTF16NoBOM extends UnicodeMetricsUTF16 {

        public UnicodeMetricsUTF16NoBOM() {
            super(BYTE_ORDER_BIG_ENDIAN);
        }

    }

    private static class UnicodeMetricsUTF16WithBOM extends UnicodeMetricsUTF16 {

        public UnicodeMetricsUTF16WithBOM() {
            super(BYTE_ORDER_BIG_ENDIAN);
        }

        public int findEnd(byte bytes[], int index, boolean includeTerminator)
                throws IOException {
            // http://en.wikipedia.org/wiki/UTF-16/UCS-2

            if (index >= bytes.length - 1)
                throw new IOException("Missing BOM.");

            int c1 = 0xff & bytes[index++];
            int c2 = 0xff & bytes[index++];
            if (c1 == 0xFF && c2 == 0xFE)
                byteOrder = BYTE_ORDER_LITTLE_ENDIAN;
            else if (c1 == 0xFE && c2 == 0xFF)
                byteOrder = BYTE_ORDER_BIG_ENDIAN;
            else
                throw new IOException("Invalid byte order mark.");

            return super.findEnd(bytes, index, includeTerminator);
        }
    }

}
