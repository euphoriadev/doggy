/*
 * Written By Charles M. Chen
 *
 * Created on Sep 2, 2005
 *
 */

package org.cmc.music.myid3;

public class MyID3v2FrameText extends MyID3v2Frame {
    public final String value;
    public final String value2;

    public MyID3v2FrameText(String frame_id, byte data_bytes[], String value) {
        this(frame_id, data_bytes, value, null);
    }

    public MyID3v2FrameText(String frame_id, byte data_bytes[], String value,
                            final String value2) {
        super(frame_id, data_bytes);
        this.value = value;
        this.value2 = value2;
    }

    public String toString() {
        return "{" + frame_id + ": " + value
                + (value2 == null ? "" : " (" + value2 + ")") + "}";
    }

}
