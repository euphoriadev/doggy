package org.cmc.music.myid3;
/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * The emulator on my machine costs about 10 seconds to rectify one file,
 * so I cut it off and simply return the original string.
 *
 */

import org.cmc.music.clean.NameRectifier;
import org.cmc.music.metadata.MusicMetadata;

public class TagFormat {
    private static final NameRectifier rectifier = new NameRectifier();

    public String processArtist(String s) {
        //modified by Romulus cause of slow execution
        return s;
        //return rectifier.rectifyArtist(s);
    }

    public String processAlbum(String s) {
        //modified by Romulus cause of slow execution
        return s;
        //return rectifier.rectifyAlbum(s);
    }

    public String processSongTitle(String s) {
        //modified by Romulus cause of slow execution
        return s;
        //return rectifier.rectifySongTitle(s);
    }

    public MusicMetadata process(MusicMetadata src) {
        MusicMetadata result = new MusicMetadata(src.name + " clean");

        result.putAll(src);

        {
            String s = src.getArtist();

            s = processArtist(s);

            result.setArtist(s);
        }
        {
            String s = src.getAlbum();

            s = processAlbum(s);

            result.setAlbum(s);
        }
        {
            String s = src.getSongTitle();

            s = processSongTitle(s);

            result.setSongTitle(s);
        }

        return result;
    }


}
