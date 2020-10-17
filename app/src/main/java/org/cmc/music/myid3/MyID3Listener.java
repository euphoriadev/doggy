/**
 *
 */
package org.cmc.music.myid3;


public abstract class MyID3Listener {

    public void log(String s, Object o) {

    }

    public void log(String s, int value) {

    }

    public void log(String s, byte value) {

    }

    public void log(String s, boolean value) {

    }

    public void log(String s, long value) {

    }

    public void log(String s, String value) {

    }

    public void logWithLength(String s, String value) {

    }

    public abstract void log(String s);

    public abstract void log();

}