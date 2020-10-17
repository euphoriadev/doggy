package org.cmc.music.common;

public class ID3ReadException extends ID3Exception {
    public ID3ReadException(String s) {
        super(s);
    }

    public ID3ReadException(String s, Exception e) {
        super(s, e);
    }
}
