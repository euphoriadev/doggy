/**
 *
 */
package org.cmc.music.fs;

public class ParsedFilename {
    public final String raw;

    private String artist = null, title = null, album = null,
            trackNumber = null;

    public ParsedFilename(final String raw) {
        this.raw = raw;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

}