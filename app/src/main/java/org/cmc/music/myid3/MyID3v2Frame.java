/*
 * Written By Charles M. Chen
 *
 * Created on Sep 2, 2005
 *
 */

package org.cmc.music.myid3;

import java.util.Comparator;

public class MyID3v2Frame {
    public final String frame_id;
    public final byte data_bytes[];

    public MyID3v2Frame(String frame_id, byte data_bytes[]) {
        this.frame_id = frame_id;
        this.data_bytes = data_bytes;
    }

    public String toString() {
        return "{" + frame_id + "}";
    }

    public static final Comparator COMPARATOR = (o1, o2) -> {
        MyID3v2Frame ph1 = (MyID3v2Frame) o1;
        MyID3v2Frame ph2 = (MyID3v2Frame) o2;
        return ph1.frame_id.compareTo(ph2.frame_id);
    };

}
