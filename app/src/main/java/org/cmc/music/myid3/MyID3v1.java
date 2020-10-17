/*
 * Written By Charles M. Chen
 *
 * Created on Jan 1, 2006
 *
 */
/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 * Set default char encoding to UTF-8
 *
 */

package org.cmc.music.myid3;

import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataConstants;

import java.io.UnsupportedEncodingException;

public class MyID3v1 implements MusicMetadataConstants {
    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    public byte[] toTag(MusicMetadata values)
            throws UnsupportedEncodingException {
        byte result[] = new byte[128];

        int index = 0;
        result[index++] = 0x54; // T
        result[index++] = 0x41; // A
        result[index++] = 0x47; // G

        writeField(result, index, 30, (String) values.get(KEY_TITLE));
        index += 30;

        writeField(result, index, 30, (String) values.get(KEY_ARTIST));
        index += 30;

        writeField(result, index, 30, (String) values.get(KEY_ALBUM));
        index += 30;

        {
            Object o = values.get(KEY_YEAR);
            Number value = null;
            if (o == null)
                ;
            else if (o instanceof Number) {
                value = (Number) values.get(KEY_YEAR);
            } else if (o instanceof String) {
                String s = (String) o;
                try {
                    value = Integer.valueOf(s);
                } catch (NumberFormatException e) {


                }
            } else


                writeField(result, index, 4, value == null ? null : "" + value);
            index += 4;
        }

        Number track_number = null;
        {
            Number value = (Number) values.get(KEY_TRACK_NUMBER);


            if (value != null && value.intValue() >= 0
                    && value.intValue() < 256)
                track_number = value;
        }

        if (track_number == null) {
            writeField(result, index, 30, (String) values.get(KEY_COMMENT));
            index += 30;
        } else {
            writeField(result, index, 28, (String) values.get(KEY_COMMENT));
            index += 28;

            result[index++] = 0;
            result[index++] = (byte) track_number.intValue();
        }

        {
            Object o = (Object) values.get(KEY_GENRE_ID);
            if (o == null)
                o = (Object) values.get(KEY_GENRE);

            if (o != null && (o instanceof String)) {
                String genre_name = (String) o;
                Number genre_id = ID3v1Genre.get(genre_name);
                if (genre_id != null) {
                    o = genre_id;


                }
            }

            if (o != null && !(o instanceof Number)) {


            } else {
                Number value = (Number) o;

                if (value != null && value.intValue() >= 0
                        && value.intValue() < 80)
                    result[index++] = (byte) value.intValue();
                else
                    result[index++] = 0;
            }
        }


        return result;
    }

    private void writeField(byte bytes[], int start, int max_length, String s)
            throws UnsupportedEncodingException {
        if (s == null) {
            for (int i = 0; i < max_length; i++)
                bytes[i + start] = 0;
            return;
        }

        byte value[] = s.getBytes(DEFAULT_CHAR_ENCODING);
        int count = Math.min(value.length, max_length);
        if (count >= 0) System.arraycopy(value, 0, bytes, start, count);
        for (int i = count; i < max_length; i++)
            bytes[i + start] = 0;
    }

//	private boolean isValidIso8859(byte bytes[], int start, int length)
//	{
//		for (int i = start; i < start + length; i++)
//		{
//			int value = 0xff & bytes[i];
//			if (value >= 0x20 && value <= 0x7E)
//				;
//			else if (value >= 0xA0 && value <= 0xFF)
//				;
//			else
//			{

//						+ " (0x" + Integer.toHexString(value) + "");
//				return false;
//			}
//		}
//		return true;
//	}

    private String getField(MyID3Listener listener, byte bytes[], int start,
                            int length) {
        for (int i = start; i < start + length; i++) {
            if (bytes[i] == 0) {
                length = i - start;
                break;
            }
        }
//		if (null != listener)
//			listener
//					.log("isValidIso8859", isValidIso8859(bytes, start, length));

        if (length > 0) {
            try {
                String result = new String(bytes, start, length,
                        DEFAULT_CHAR_ENCODING);
                result = result.trim();
                if (result.length() < 1)
                    return null;
                return result;
            } catch (Throwable e) {

            }
        }

        return null;
    }

    public MusicMetadata parseTags(byte bytes[]) {
        return parseTags(null, bytes);
    }

    public MusicMetadata parseTags(MyID3Listener listener, byte bytes[]) {
        MusicMetadata tags = new MusicMetadata("id3v1");

        int counter = 3;
        String title = getField(listener, bytes, counter, 30);
        counter += 30;
        tags.put(KEY_TITLE, title);
        if (null != listener)
            listener.logWithLength("id3v1 title", title);

        String artist = getField(listener, bytes, counter, 30);
        counter += 30;
        tags.put(KEY_ARTIST, artist);
        if (null != listener)
            listener.logWithLength("id3v1 artist", artist);

        String album = getField(listener, bytes, counter, 30);
        counter += 30;
        tags.put(KEY_ALBUM, album);
        if (null != listener)
            listener.logWithLength("id3v1 album", album);

        String year = getField(listener, bytes, counter, 4);
        counter += 4;
        tags.put(KEY_YEAR, year);
        if (null != listener)
            listener.logWithLength("id3v1 year", year);

        String comment = getField(listener, bytes, counter, 30);
        counter += 30;
        tags.put(KEY_COMMENT, comment);
        if (null != listener)
            listener.logWithLength("id3v1 comment", comment);

        if (bytes[counter - 2] == 0 && bytes[counter - 1] != 0) {
            int trackNumber = 0xff & bytes[counter - 1];
            tags.put(KEY_TRACK_NUMBER, trackNumber);

            if (null != listener)
                listener.log("id3v1 trackNumber: " + trackNumber);
        }

        int genre = 0xff & bytes[counter];
        if (genre < 80 && genre > 0) {
            tags.put(KEY_GENRE_ID, genre);

            if (null != listener)
                listener.log("id3v1 genre: " + genre);
        }

        if (null != listener)
            listener.log();

        return tags;
    }

}
