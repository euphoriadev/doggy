/**
 *
 */

/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 *
 */

package org.cmc.music.fs;

import org.cmc.music.clean.NameRectifier;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.myid3.TagFormat;
import org.cmc.music.util.TextUtils;

import java.io.File;

public abstract class FSParser {

    private static final TagFormat utils = new TagFormat();

    private static final NameRectifier nameRectifier = new NameRectifier();

    public static final ParsedFilename parseFolder(File file) {
        return parseFolder(file.getName());
    }

    public static final ParsedFilename parseFolder(String s) {
        ParsedFilename result = new ParsedFilename(s);

        int hyphen_count = TextUtils.split(s, "-").length - 1;
        if (hyphen_count != 1)
            return result;

        String artist = s.substring(0, s.indexOf('-'));
        String album = s.substring(s.indexOf('-') + 1);

        artist = nameRectifier.rectifyArtist(artist);
        album = nameRectifier.rectifyAlbum(album);

        if (artist == null && album == null)
            return result;

        result.setArtist(artist);
        result.setAlbum(album);

        return result;
    }

    public static final boolean isTrackNumber(String s) {
        if (s == null)
            return false;
        s = s.trim();

        if (s.length() < 1 || s.length() > 3)
            return false;

        if (TextUtils.kALPHABET.indexOf(s.charAt(0)) >= 0) {
            // first letter can be letter.
            s = s.substring(1);
            if (s.length() < 1)
                return false;
        }
        return TextUtils.filter(s, TextUtils.kALPHABET_NUMERALS).equals(s);
    }

    private static Number getTrackNumber(String s) {
        try {
            return Integer.valueOf(s);
        } catch (Throwable e) {

            return null;
        }
    }

    public static final MusicMetadata parseFilename(String fileName,
                                                    String folderName) {
        if (fileName == null)
            return null;

        if (!fileName.toLowerCase().endsWith(".mp3"))
            return null;
        fileName = fileName.substring(0, fileName.length() - 4);

        String artist;
        String song_title;
        Number track_number;

        String splits[] = fileName.split("-");

        if (splits.length == 2) {
            if (FSParser.isTrackNumber(splits[0])) {
                artist = null;
                track_number = getTrackNumber(splits[0].trim());
                song_title = utils.processSongTitle(splits[1]);
            } else if (FSParser.isTrackNumber(splits[1])) {
                artist = null;
                song_title = utils.processSongTitle(splits[0]);
                track_number = getTrackNumber(splits[1].trim());
            } else {
                artist = utils.processArtist(splits[0]);
                song_title = utils.processSongTitle(splits[1]);
                track_number = null;
            }
        } else if (splits.length == 3) {
            if (FSParser.isTrackNumber(splits[0])) {
                track_number = getTrackNumber(splits[0].trim());
                artist = utils.processArtist(splits[1]);
                song_title = utils.processSongTitle(splits[2]);
            } else if (FSParser.isTrackNumber(splits[1])) {
                artist = utils.processArtist(splits[0]);
                track_number = getTrackNumber(splits[1].trim());
                song_title = utils.processSongTitle(splits[2]);
            } else
                return null;
        } else
            return null;


        if (FSParser.isTrackNumber(artist)) {

            return null;
        }

        if (FSParser.isTrackNumber(song_title)) {

            return null;
        }


        if (folderName != null && folderName.endsWith("(!)"))
            folderName = folderName.substring(0, folderName.length() - 3);

        String kVariousArtists = "Various Artists";
        String album = null;
        if (folderName != null && !folderName.startsWith("@")) {
            if (artist != null) {
                if (folderName.toLowerCase().startsWith(
                        kVariousArtists.toLowerCase()))
                    folderName = folderName.substring(kVariousArtists.length());
                else if (folderName.toLowerCase().startsWith(
                        artist.toLowerCase()))
                    folderName = folderName.substring(artist.length());
                else if (folderName.toLowerCase()
                        .endsWith(artist.toLowerCase()))
                    folderName = folderName.substring(0, folderName.length()
                            - artist.length());
                else {

                    return null;
                }


                album = utils.processAlbum(folderName);
            } else {
                int first_hyphen = folderName.indexOf('-');
                int last_hyphen = folderName.lastIndexOf('-');
                if (first_hyphen >= 0 && first_hyphen == last_hyphen) {
                    artist = utils.processArtist(folderName.substring(0,
                            first_hyphen));
                    album = utils.processAlbum(folderName
                            .substring(first_hyphen + 1));
                } else
                    return null;
            }

        }


        if (artist == null)
            return null;
        if (artist.equalsIgnoreCase(kVariousArtists))
            artist = null;

        MusicMetadata result = new MusicMetadata("filename");

        result.setAlbum(album);
        result.setArtist(artist);
        result.setSongTitle(song_title);
        if (track_number != null)
            result.setTrackNumber(track_number);
        //		result.getTrackNumber()

        return result;
    }
}