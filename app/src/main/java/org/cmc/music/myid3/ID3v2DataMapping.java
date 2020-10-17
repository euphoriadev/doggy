/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 *
 */

package org.cmc.music.myid3;

import org.cmc.music.common.ID3FrameType;
import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ID3v2DataMapping implements MusicMetadataConstants {

    private static abstract class ID3v2TagHandler {
        protected abstract ID3FrameType getFrameType();

        protected abstract Object getKey();

        public boolean matches(String s) {
            return getFrameType().matches(s);
        }

        public void process(MusicMetadata values, MyID3v2FrameText tag) {
            Object key = getKey();
            if (key == null)
                return;
            values.put(key, tag.value);
        }

    }

    private static final ID3v2TagHandler handlers[] = {

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.COMMENT;
                }

                protected Object getKey() {
                    return KEY_COMMENT;
                }

            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.ALBUM;
                }

                protected Object getKey() {
                    return KEY_ALBUM;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.ARTIST;
                }

                protected Object getKey() {
                    return KEY_ARTIST;
                }
            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.TITLE;
                }

                protected Object getKey() {
                    return KEY_TITLE;
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.CONTENTTYPE;
                }

                protected Object getKey() {
                    return null;
                }

                public void process(MusicMetadata values, MyID3v2FrameText tag) {
                    // matthew charles: actually, TCON are of form (1)(2)refinement...
                    // should catch or at least warn of refinements, multiples values

                    String value = tag.value;
                    try {
                        if (value == null || value.trim().length() < 1)
                            return;

                        boolean id_only = value.matches("^\\(\\d+\\)");

                        if (id_only) {
                            int index = value.indexOf(')');
                            String number = value.substring(1, index);


                            number = number.trim();
                            if (isNumber(number)) {
                                Number id = new Integer(number);
                                if (id.intValue() != 0) {
                                    values.put(KEY_GENRE_ID, id);
                                    String genre = ID3v1Genre.get(id);
                                    if (null != genre)
                                        values.put(KEY_GENRE, genre);
                                }
                                value = value.substring(index + 1);
                            }
                        } else {
                            boolean numeric_only = value.matches("^\\d+$");

                            if (numeric_only) {
                                Number id = Integer.valueOf(value);
                                if (id.intValue() != 0) {
                                    values.put(KEY_GENRE_ID, id);
                                    String genre = ID3v1Genre.get(id);
                                    if (null != genre)
                                        values.put(KEY_GENRE, genre);
                                }
                                value = "";

                            }
                        }

                        //							else
                        if (value.length() > 0) {
                            values.put(KEY_GENRE, value);
                        }
                    } catch (Throwable e) {

                    }
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.PUBLISHER;
                }

                protected Object getKey() {
                    return KEY_PUBLISHER;
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.YEAR;
                }

                protected Object getKey() {
                    return KEY_YEAR;
                }

                public void process(MusicMetadata values, MyID3v2FrameText tag)

                {
                    try {
                        String value = tag.value;
                        if (value == null || value.trim().length() < 1)
                            return;
                        value = value.trim();
                        if (!isNumber(value))
                            return;
                        Number number = Integer.valueOf(value);
                        values.put(KEY_YEAR, number);
                    } catch (Throwable e) {

                    }
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.TRACKNUM;
                }

                protected Object getKey() {
                    return KEY_TRACK_NUMBER;
                }

                public void process(MusicMetadata values, MyID3v2FrameText tag)

                {
                    try {
                        String value = tag.value;
                        if (value == null || value.trim().length() < 1)
                            return;


                        if (value.indexOf('/') >= 0) {
                            try {
                                String s = value
                                        .substring(value.indexOf('/') + 1);
                                s = s.trim();
                                if (isNumber(s)) {
                                    Number track_count = new Integer(s);
                                    values.put(KEY_TRACK_COUNT, track_count);
                                }
                            } catch (Throwable e) {

                            }
                            value = value.substring(0, value.indexOf('/'));

                        }

                        value = value.trim();
                        if (isNumber(value)) {
                            Number number = new Integer(value);
                            values.put(KEY_TRACK_NUMBER, number);
                        }
                    } catch (Throwable e) {

                    }
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.SONGLEN;
                }

                protected Object getKey() {
                    return KEY_DURATION_SECONDS;
                }

                public void process(MusicMetadata values, MyID3v2FrameText tag)

                {
                    try {
                        String value = tag.value;
                        if (value == null || value.trim().length() < 1)
                            return;


                        Number number = new Long(value);


                        // ms to seconds
                        number = number.longValue() / 1000;

                        if (number.intValue() == 0)
                            return;

                        values.put(KEY_DURATION_SECONDS, number);
                    } catch (Throwable e) {

                    }
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.COMPOSER;
                }

                protected Object getKey() {
                    return KEY_COMPOSER;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.CONDUCTOR;
                }

                protected Object getKey() {
                    return KEY_CONDUCTOR;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.BAND;
                }

                protected Object getKey() {
                    return KEY_BAND;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.MIXARTIST;
                }

                protected Object getKey() {
                    return KEY_MIX_ARTIST;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.LYRICIST;
                }

                protected Object getKey() {
                    return KEY_LYRICIST;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.USERTEXT;
                }

                protected Object getKey() {
                    return null;
                }

                public void process(MusicMetadata values, MyID3v2FrameText tag) {
                    if (tag.value == null || tag.value2 == null)
                        return;

                    String key = tag.value;
                    String value = tag.value2;
                    if (key.equalsIgnoreCase("engineer"))
                        values.put(KEY_ENGINEER, value);
                    else if (key.equalsIgnoreCase("Rip date"))
                        ;
                    else if (key.equalsIgnoreCase("Ripping tool"))
                        ;
                    else if (key.equalsIgnoreCase("TraktorID"))
                        ;
                    else if (key.equalsIgnoreCase("TraktorPeakDB"))
                        ;
                    else if (key.equalsIgnoreCase("TraktorPerceivedDB"))
                        ;
                        //					else if (key.equalsIgnoreCase("fBPM"))
                        //						;
                    else if (key.equalsIgnoreCase("fBPMQuality"))
                        ;
                    else if (key.equalsIgnoreCase("TraktorReleaseDate"))
                        ;
                        //					else if (key.equalsIgnoreCase("Ripping tool"))
                        //						;
                    else {


                    }
                    //					if(tag.value)
                    //					if(tag.v)
                }
            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.ENCODEDBY;
                }

                protected Object getKey() {
                    return null;
                }

            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.ENCODERSETTINGS;
                }

                protected Object getKey() {
                    return null;
                }

            }, //

            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.MEDIATYPE;
                }

                protected Object getKey() {
                    return null;
                }

            }, //
            new ID3v2TagHandler() {
                protected ID3FrameType getFrameType() {
                    return ID3FrameType.FILETYPE;
                }

                protected Object getKey() {
                    return null;
                }

            }, //

    };

    private static final Map keyToFrameTypeMap = new HashMap();
    private static final Vector ignoredFrameTypes = new Vector();

    static {
        for (ID3v2TagHandler handler : handlers) {
            Object key = handler.getKey();
            if (key != null)
                keyToFrameTypeMap.put(key, handler.getFrameType());
            else
                ignoredFrameTypes.add(handler.getFrameType());
        }
    }

    public ID3FrameType getID3FrameType(Object key) {
        if (key.equals(KEY_PICTURES))
            return ID3FrameType.PICTURE;

        return (ID3FrameType) keyToFrameTypeMap.get(key);
    }

    public MusicMetadata process(Vector tags) {
        if (tags == null)
            return null;

        try {
            MusicMetadata result = new MusicMetadata("id3v2");

            for (int i = 0; i < tags.size(); i++) {
                Object o = (Object) tags.get(i);
                if (o instanceof MyID3v2FrameImage) {
                    MyID3v2FrameImage imageFrame = (MyID3v2FrameImage) o;

                    ImageData imageData = imageFrame.getImageData();
                    result.addPicture(imageData);

                    continue;
                }
                if (!(o instanceof MyID3v2FrameText))
                    continue;
                MyID3v2FrameText tag = (MyID3v2FrameText) tags.get(i);
                //					process(tag);
                process(result, tag);
            }

            return result;
        } catch (Throwable e) {


            return null;
        }
    }

    private void process(MusicMetadata values, MyID3v2FrameText tag) {


        for (ID3v2TagHandler handler : handlers) {
            if (!handler.matches(tag.frame_id))
                continue;
            handler.process(values, tag);
            return;
        }


    }

    private static boolean isNumber(String s) {
        return s.matches("^-?[0-9]+$");
    }

}
