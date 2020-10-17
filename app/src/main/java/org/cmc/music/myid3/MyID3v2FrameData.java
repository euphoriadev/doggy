/*
 * Written By Charles M. Chen
 *
 * Created on Sep 2, 2005
 *
 */

package org.cmc.music.myid3;

public class MyID3v2FrameData extends MyID3v2Frame {
    public final ID3v2FrameFlags flags;

    public MyID3v2FrameData(String frame_id, byte data_bytes[],
                            final ID3v2FrameFlags flags) {
        super(frame_id, data_bytes);
        this.flags = flags;
    }

    public String toString() {
        return "{" + frame_id + ": " + data_bytes.length + "}";
    }

}
