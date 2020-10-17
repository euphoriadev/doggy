package org.cmc.music.myid3;

import org.cmc.music.metadata.MusicMetadata;

import java.util.Vector;

public class ID3Tag {
    public static final int TAG_TYPE_ID3_V1 = 1;
    public static final int TAG_TYPE_ID3_V2 = 2;

    public final int tag_type;
    public final byte bytes[];
    public final MusicMetadata values;

    public ID3Tag(int tag_type, byte[] bytes, MusicMetadata values) {
        this.tag_type = tag_type;
        this.bytes = bytes;
        this.values = values;
    }

    public static class V1 extends ID3Tag {
        public V1(byte[] bytes, MusicMetadata values) {
            super(ID3Tag.TAG_TYPE_ID3_V1, bytes, values);
        }
    }

    public static class V2 extends ID3Tag {
        public final Vector frames;
        public final byte version_major;
        public final byte version_minor;

        public V2(final byte version_major, final byte version_minor,
                  byte[] bytes, MusicMetadata values, final Vector frames) {
            super(ID3Tag.TAG_TYPE_ID3_V2, bytes, values);

            this.version_major = version_major;
            this.version_minor = version_minor;
            this.frames = frames;
        }


    }

    public String toString() {
        return "{ID3Tag. " +
                "values: " + values +
                " }";
    }

}
