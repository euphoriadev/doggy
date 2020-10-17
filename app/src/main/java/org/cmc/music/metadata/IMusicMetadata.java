package org.cmc.music.metadata;

import java.util.Vector;

public interface IMusicMetadata {

    public abstract String getSongTitle();

    public abstract String getArtist();

    public abstract String getAlbum();

    public abstract String getYear();

    public abstract String getComment();

    public abstract Number getTrackNumber();

    public abstract String getGenre();

    public abstract String getDurationSeconds();

    public abstract String getComposer();

    public abstract String getProducerArtist();

    public abstract String getComposer2();

    public abstract String getCompilation();

    public abstract void clearSongTitle();

    public abstract void clearArtist();

    public abstract void clearAlbum();

    public abstract void clearYear();

    public abstract void clearComment();

    public abstract void clearTrackNumber();

    public abstract void clearGenre();

    public abstract void clearDurationSeconds();

    public abstract void clearComposer();

    public abstract void clearProducerArtist();

    public abstract void clearComposer2();

    public abstract void clearCompilation();

    public abstract void clearFeaturingList();

    public abstract void setFeaturingList(Vector v);

    public abstract Vector getFeaturingList();

    public abstract void setSongTitle(String s);

    public abstract void setArtist(String s);

    public abstract void setAlbum(String s);

    public abstract void setYear(String s);

    public abstract void setComment(String s);

    public abstract void setTrackNumber(Number s);

    public abstract void setGenre(String s);

    public abstract void setDurationSeconds(String s);

    public abstract void setComposer(String s);

    public abstract void setProducerArtist(String s);

    public abstract void setComposer2(String s);

    public abstract void setCompilation(String s);

    public abstract String getProducer();

    public abstract void setProducer(String s);

    public abstract void clearProducer();

}