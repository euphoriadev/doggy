package org.cmc.music.common;

public abstract class ID3Exception extends Exception {
    public ID3Exception(String s) {
        super(s);
    }

    public ID3Exception(String s, Exception e) {
        super(s, e);
    }
}
