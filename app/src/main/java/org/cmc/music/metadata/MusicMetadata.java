/*
 * Written By Charles M. Chen
 *
 * Created on Jan 22, 2006
 *
 */

/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 * Changed SimpleMap importing from sharedlib from modified music.util
 *
 */

package org.cmc.music.metadata;

import org.cmc.music.util.SimpleMap;

import java.util.Collections;
import java.util.Vector;


/**
 * A collection of metadata values, possibly read from a single tag (ie. ID3v1 or ID3v2)
 *
 * @see org.cmc.music.myid3.metadata.MusicMetadataSet
 */
public class MusicMetadata extends SimpleMap
        implements
        MusicMetadataConstants,
        IMusicMetadata {
    public final String name;

    public MusicMetadata(String name) {
        this.name = name;
    }

    public MusicMetadata(MusicMetadata other) {
        this.name = other.name;
        putAll(other);
    }

    public static final MusicMetadata createEmptyMetadata() {
        return new MusicMetadata("New Metadata");
    }

    public boolean hasBasicInfo() {
        if (null == getArtist())
            return false;
        if (null == getSongTitle())
            return false;
        if (null == getAlbum())
            return false;
        return null != getTrackNumber();
    }

    private Number getNumber(Object key) {
        Object result = get(key);
        if (result == null)
            return null;


        return (Number) result;
    }

    private String getString(Object key) {
        Object result = get(key);
        if (result == null)
            return null;


        return (String) result;
    }

    private Vector getVector(Object key) {
        Object result = get(key);
        if (result == null)
            return null;


        return (Vector) result;
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getSongTitle()
     */
    public String getSongTitle() {
        return getString(KEY_TITLE);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getArtist()
     */
    public String getArtist() {
        return getString(KEY_ARTIST);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getAlbum()
     */
    public String getAlbum() {
        return getString(KEY_ALBUM);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getYear()
     */
    public String getYear() {
        return getString(KEY_YEAR);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getComment()
     */
    public String getComment() {
        return getString(KEY_COMMENT);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getTrackNumber()
     */
    public Number getTrackNumber() {
        return getNumber(KEY_TRACK_NUMBER);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getGenre()
     */
    public String getGenre() {
        return getString(KEY_GENRE);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getDurationSeconds()
     */
    public String getDurationSeconds() {
        return getString(KEY_DURATION_SECONDS);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getComposer()
     */
    public String getComposer() {
        return getString(KEY_COMPOSER);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getProducerArtist()
     */
    public String getProducerArtist() {
        return getString(KEY_ALBUM_ARTIST);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getComposer2()
     */
    public String getComposer2() {
        return getString(KEY_COMPOSER_2);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getCompilation()
     */
    public String getCompilation() {
        return getString(KEY_COMPILATION);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearSongTitle()
     */
    public void clearSongTitle() {
        remove(KEY_TITLE);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearArtist()
     */
    public void clearArtist() {
        remove(KEY_ARTIST);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearAlbum()
     */
    public void clearAlbum() {
        remove(KEY_ALBUM);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearYear()
     */
    public void clearYear() {
        remove(KEY_YEAR);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearComment()
     */
    public void clearComment() {
        remove(KEY_COMMENT);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearTrackNumber()
     */
    public void clearTrackNumber() {
        remove(KEY_TRACK_NUMBER);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearGenre()
     */
    public void clearGenre() {
        remove(KEY_GENRE);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearDurationSeconds()
     */
    public void clearDurationSeconds() {
        remove(KEY_DURATION_SECONDS);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearComposer()
     */
    public void clearComposer() {
        remove(KEY_COMPOSER);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearProducerArtist()
     */
    public void clearProducerArtist() {
        remove(KEY_ALBUM_ARTIST);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearComposer2()
     */
    public void clearComposer2() {
        remove(KEY_COMPOSER_2);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearCompilation()
     */
    public void clearCompilation() {
        remove(KEY_COMPILATION);
    }

    //

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearFeaturingList()
     */
    public void clearFeaturingList() {
        remove(KEY_FEATURING_LIST);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setFeaturingList(java.util.Vector)
     */
    public void setFeaturingList(Vector v) {
        put(KEY_FEATURING_LIST, v);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getFeaturingList()
     */
    public Vector getFeaturingList() {
        return getVector(KEY_FEATURING_LIST);
    }

    //

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearFeaturingList()
     */
    public void clearPictureList() {
        remove(KEY_PICTURES);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setFeaturingList(java.util.Vector)
     */
    public void setPictureList(Vector v) {
        put(KEY_PICTURES, v);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getFeaturingList()
     */
    public Vector getPictureList() {
        Vector result = getVector(KEY_PICTURES);
        if (result == null)
            result = new Vector();
        return result;
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getFeaturingList()
     */
    public void addPicture(ImageData image) {
        Vector v = getVector(KEY_PICTURES);
        if (null == v)
            v = new Vector();
        v.add(image);
        put(KEY_PICTURES, v);
    }

    //

    /**
     * (non-Javadoc)
     *
     * @see org.cmc.music.common.IMusicMetadata#setSongTitle(java.lang.String)
     */
    public void setSongTitle(String s) {
        put(KEY_TITLE, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setArtist(java.lang.String)
     */
    public void setArtist(String s) {
        put(KEY_ARTIST, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setAlbum(java.lang.String)
     */
    public void setAlbum(String s) {
        put(KEY_ALBUM, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setYear(java.lang.String)
     */
    public void setYear(String s) {
        put(KEY_YEAR, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setComment(java.lang.String)
     */
    public void setComment(String s) {
        put(KEY_COMMENT, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setTrackNumber(java.lang.Number)
     */
    public void setTrackNumber(Number s) {
        put(KEY_TRACK_NUMBER, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setGenre(java.lang.String)
     */
    public void setGenre(String s) {
        put(KEY_GENRE, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setDurationSeconds(java.lang.String)
     */
    public void setDurationSeconds(String s) {
        put(KEY_DURATION_SECONDS, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setComposer(java.lang.String)
     */
    public void setComposer(String s) {
        put(KEY_COMPOSER, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setProducerArtist(java.lang.String)
     */
    public void setProducerArtist(String s) {
        put(KEY_ALBUM_ARTIST, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setComposer2(java.lang.String)
     */
    public void setComposer2(String s) {
        put(KEY_COMPOSER_2, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setCompilation(java.lang.String)
     */
    public void setCompilation(String s) {
        put(KEY_COMPILATION, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#getProducer()
     */
    public String getProducer() {
        return getString(KEY_PRODUCER);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#setProducer(java.lang.String)
     */
    public void setProducer(String s) {
        put(KEY_PRODUCER, s);
    }

    /* (non-Javadoc)
     * @see org.cmc.music.common.IMusicMetadata#clearProducer()
     */
    public void clearProducer() {
        remove(KEY_PRODUCER);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{ ");

        Vector keys = new Vector(keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            Object value = get(key);

            if (i > 0)
                result.append(", ");
            result.append(key + ": " + value);
        }

        result.append(" }");

        return result.toString();
    }

}
