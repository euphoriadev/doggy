package org.cmc.music.common;

public class ID3WriteException extends ID3Exception {
    public ID3WriteException(String s) {
        super(s);
    }

    public ID3WriteException(String s, Exception e) {
        super(s, e);
    }
}
