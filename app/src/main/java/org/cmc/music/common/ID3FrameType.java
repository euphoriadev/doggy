/*
 * Written By Charles M. Chen
 *
 * Created on Sep 2, 2005
 *
 */

/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 *
 */

package org.cmc.music.common;

import java.util.Hashtable;
import java.util.Map;

public class ID3FrameType {

    public final String short_id;
    public final String long_id;
    public final String description;
    private final Number frame_order;

    public static final Number DEFAULT_FRAME_ORDER = Integer.MAX_VALUE;
    public static final Number TEXT_FRAME_ORDER = Integer.MAX_VALUE / 2;

    private static Number getFrameOrder(String short_id, String long_id_1,
                                        String long_id_2) {
        try {

            String frameid = long_id_1;
            if (frameid == null) frameid = long_id_2;
            if (frameid == null) frameid = short_id;
            if (frameid == null) return DEFAULT_FRAME_ORDER;

            return frameid.charAt(0) == 'T'
                    ? TEXT_FRAME_ORDER
                    : DEFAULT_FRAME_ORDER;
        } catch (Throwable e) {
            return DEFAULT_FRAME_ORDER;
        }
    }

    public ID3FrameType(String short_id, String long_id_1, String long_id_2,
                        String description) {
        this(short_id, long_id_1, long_id_2, description, getFrameOrder(
                short_id, long_id_1, long_id_2));
    }

    public ID3FrameType(String short_id, String long_id_1, String long_id_2,
                        String description, Number frame_order) {
        //		try
        //		{
        this.frame_order = frame_order;
        this.description = description;
        if ((short_id == null) || short_id.length() == 0)
            short_id = null;
        if ((long_id_1 == null) || long_id_1.length() == 0)
            long_id_1 = null;
        if ((long_id_2 == null) || long_id_2.length() == 0)
            long_id_2 = null;
        this.short_id = short_id;
        this.long_id = (long_id_1 == null) ? long_id_2 : long_id_1;

        if ((long_id_1 != null) && (long_id_2 != null)
                && (long_id_1 != long_id_2))
            throw new Error("long_id_1: " + long_id_1 + ", long_id_2: "
                    + long_id_2);
        //		}
        //		catch (Error e)
        //		{

        //			throw e;
        //		}
    }

    public ID3FrameType(String frame_id, String description) {
        this.description = description;
        this.long_id = frame_id;
        this.short_id = null;
        this.frame_order = DEFAULT_FRAME_ORDER;
    }

    public Number getFrameOrder() {
        return frame_order;
    }

    public String toString() {
        return "{ " + short_id + " / " + long_id + ": " + description + " }";
    }

    public boolean matches(String s) {
        return ((long_id != null) && long_id.equalsIgnoreCase(s))
                || ((short_id != null) && short_id.equalsIgnoreCase(s));
    }

    public static final ID3FrameType GENRE_ID = new ID3FrameType(null, null,
            null, "Genre Id");
    public static final ID3FrameType CONTENT_GROUP = new ID3FrameType("TT1",
            "TIT1", "TIT1", "Content Group Description");
    public static final ID3FrameType TITLE = new ID3FrameType("TT2", "TIT2",
            "TIT2", "Title/Songname/Content Description", 2);
    public static final ID3FrameType SUBTITLE = new ID3FrameType("TT3", "TIT3",
            "TIT3", "Subtitle/Description Refinement");
    public static final ID3FrameType ARTIST = new ID3FrameType("TP1", "TPE1",
            "TPE1", "Lead Performer(S)/Soloist(S)", 2);
    public static final ID3FrameType BAND = new ID3FrameType("TP2", "TPE2",
            "TPE2", "Band/Orchestra/Accompaniment", 2);
    public static final ID3FrameType CONDUCTOR = new ID3FrameType("TP3",
            "TPE3", "TPE3", "Conductor/Performer Refinement");
    public static final ID3FrameType MIXARTIST = new ID3FrameType("TP4",
            "TPE4", "TPE4", "Interpreted, Remixed, Modified By");
    public static final ID3FrameType COMPOSER = new ID3FrameType("TCM", "TCOM",
            "TCOM", "Composer");
    public static final ID3FrameType LYRICIST = new ID3FrameType("TXT", "TEXT",
            "TEXT", "Lyricist/Text Writer");
    public static final ID3FrameType LANGUAGE = new ID3FrameType("TLA", "TLAN",
            "TLAN", "Language(S)");
    public static final ID3FrameType CONTENTTYPE = new ID3FrameType("TCO",
            "TCON", "TCON", "Content Type");
    public static final ID3FrameType ALBUM = new ID3FrameType("TAL", "TALB",
            "TALB", "Album/Movie/Show Title");
    public static final ID3FrameType TRACKNUM = new ID3FrameType("TRK", "TRCK",
            "TRCK", "Track Number/Position In Set", 4);
    public static final ID3FrameType PARTINSET = new ID3FrameType("TPA",
            "TPOS", "TPOS", "Part Of Set");
    public static final ID3FrameType ISRC = new ID3FrameType("TRC", "TSRC",
            "TSRC", "International Standard Recording Code");
    public static final ID3FrameType DATE = new ID3FrameType("TDA", "TDAT",
            null, "Date");
    public static final ID3FrameType YEAR = new ID3FrameType("TYE", "TYER",
            null, "Year");
    public static final ID3FrameType TIME = new ID3FrameType("TIM", "TIME",
            null, "Time");
    public static final ID3FrameType RECORDINGDATES = new ID3FrameType("TRD",
            "TRDA", null, "Recording Dates");
    public static final ID3FrameType RECORDINGTIME = new ID3FrameType(null,
            null, "TDRC", "Recording Time");
    public static final ID3FrameType ORIGYEAR = new ID3FrameType("TOR", "TORY",
            null, "Original Release Year");
    public static final ID3FrameType ORIGRELEASETIME = new ID3FrameType(null,
            null, "TDOR", "Original Release Time");
    public static final ID3FrameType BPM = new ID3FrameType("TBP", "TBPM",
            "TBPM", "Beats Per Minute");
    public static final ID3FrameType MEDIATYPE = new ID3FrameType("TMT",
            "TMED", "TMED", "Media Type");
    public static final ID3FrameType FILETYPE = new ID3FrameType("TFT", "TFLT",
            "TFLT", "File Type");
    public static final ID3FrameType COPYRIGHT = new ID3FrameType("TCR",
            "TCOP", "TCOP", "Copyright Message");
    public static final ID3FrameType PUBLISHER = new ID3FrameType("TPB",
            "TPUB", "TPUB", "Publisher");
    public static final ID3FrameType ENCODEDBY = new ID3FrameType("TEN",
            "TENC", "TENC", "Encoded By");
    public static final ID3FrameType ENCODERSETTINGS = new ID3FrameType("TSS",
            "TSSE", "TSSE", "Software/Hardware + Settings For Encoding");
    public static final ID3FrameType SONGLEN = new ID3FrameType("TLE", "TLEN",
            "TLEN", "Length (Ms)");
    public static final ID3FrameType SIZE = new ID3FrameType("TSI", "TSIZ",
            null, "Size (Bytes)");
    public static final ID3FrameType PLAYLISTDELAY = new ID3FrameType("TDY",
            "TDLY", "TDLY", "Playlist Delay");
    public static final ID3FrameType INITIALKEY = new ID3FrameType("TKE",
            "TKEY", "TKEY", "Initial Key");
    public static final ID3FrameType ORIGALBUM = new ID3FrameType("TOT",
            "TOAL", "TOAL", "Original Album/Movie/Show Title");
    public static final ID3FrameType ORIGFILENAME = new ID3FrameType("TOF",
            "TOFN", "TOFN", "Original Filename");
    public static final ID3FrameType ORIGARTIST = new ID3FrameType("TOA",
            "TOPE", "TOPE", "Original Artist(S)/Performer(S)");
    public static final ID3FrameType ORIGLYRICIST = new ID3FrameType("TOL",
            "TOLY", "TOLY", "Original Lyricist(S)/Text Writer(S)");
    public static final ID3FrameType FILEOWNER = new ID3FrameType(null, "TOWN",
            "TOWN", "File Owner/Licensee");
    public static final ID3FrameType NETRADIOSTATION = new ID3FrameType(null,
            "TRSN", "TRSN", "Internet Radio Station Name");
    public static final ID3FrameType NETRADIOOWNER = new ID3FrameType(null,
            "TRSO", "TRSO", "Internet Radio Station Owner");
    public static final ID3FrameType SETSUBTITLE = new ID3FrameType(null, null,
            "TSST", "Set Subtitle");
    public static final ID3FrameType MOOD = new ID3FrameType(null, null,
            "TMOO", "Mood");
    public static final ID3FrameType PRODUCEDNOTICE = new ID3FrameType(null,
            null, "TPRO", "Produced Notice");
    public static final ID3FrameType ENCODINGTIME = new ID3FrameType(null,
            null, "TDEN", "Encoding Time");
    public static final ID3FrameType RELEASETIME = new ID3FrameType(null, null,
            "TDRL", "Release Time");
    public static final ID3FrameType TAGGINGTIME = new ID3FrameType(null, null,
            "TDTG", "Tagging Time");
    public static final ID3FrameType ALBUMSORTORDER = new ID3FrameType(null,
            null, "TSOA", "Album Sort Order");
    public static final ID3FrameType PERFORMERSORTORDER = new ID3FrameType(
            null, null, "TSOP", "Performer Sort Order");
    public static final ID3FrameType TITLESORTORDER = new ID3FrameType(null,
            null, "TSOT", "Title Sort Order");
    public static final ID3FrameType USERTEXT = new ID3FrameType("TXX", "TXXX",
            "TXXX", "User Defined Text Information Frame");
    public static final ID3FrameType WWWAUDIOFILE = new ID3FrameType("WAF",
            "WOAF", "WOAF", "Official Audio File Webpage");
    public static final ID3FrameType WWWARTIST = new ID3FrameType("WAR",
            "WOAR", "WOAR", "Official Artist/Performer Webpage");
    public static final ID3FrameType WWWAUDIOSOURCE = new ID3FrameType("WAS",
            "WOAS", "WOAS", "Official Audion Source Webpage");
    public static final ID3FrameType WWWCOMMERCIALINFO = new ID3FrameType(
            "WCM", "WCOM", "WCOM", "Commercial Information");
    public static final ID3FrameType WWWCOPYRIGHT = new ID3FrameType("WCP",
            "WCOP", "WCOP", "Copyright/Legal Information");
    public static final ID3FrameType WWWPUBLISHER = new ID3FrameType("WPB",
            "WPUB", "WPUB", "Publishers Official Webpage");
    public static final ID3FrameType WWWRADIOPAGE = new ID3FrameType(null,
            "WORS", "WORS", "Official Internet Radio Station Homepage");
    public static final ID3FrameType WWWPAYMENT = new ID3FrameType(null,
            "WPAY", "WPAY", "Payment");
    public static final ID3FrameType WWWUSER = new ID3FrameType("WXX", "WXXX",
            "WXXX", "User Defined Url Link Frame");
    public static final ID3FrameType INVOLVEDPEOPLE = new ID3FrameType("IPL",
            "IPLS", null, "Involved People List");
    public static final ID3FrameType MUSICIANCREDITLIST = new ID3FrameType(
            null, null, "TMCL", "Musician Credits List");
    public static final ID3FrameType kINVOLVEDPEOPLE2 = new ID3FrameType(null,
            null, "TIPL", "Involved People List");
    public static final ID3FrameType UNSYNCEDLYRICS = new ID3FrameType("ULT",
            "USLT", "USLT", "Unsynchronised Lyrics/Text Transcription");
    public static final ID3FrameType COMMENT = new ID3FrameType("COM", "COMM",
            "COMM", "Comments");
    public static final ID3FrameType TERMSOFUSE = new ID3FrameType(null,
            "USER", "USER", "Terms Of Use");
    public static final ID3FrameType UNIQUEFILEID = new ID3FrameType("UFI",
            "UFID", "UFID", "Unique File Identifier", 1);
    public static final ID3FrameType CDID = new ID3FrameType("MCI", "MCDI",
            "MCDI", "Music Cd Identifier", 3);
    public static final ID3FrameType EVENTTIMING = new ID3FrameType("ETC",
            "ETCO", "ETCO", "Event Timing Codes");
    public static final ID3FrameType MPEGLOOKUP = new ID3FrameType("MLL",
            "MLLT", "MLLT", "Mpeg Location Lookup Table");
    public static final ID3FrameType SYNCEDTEMPO = new ID3FrameType("STC",
            "SYTC", "SYTC", "Synchronised Tempo Codes");
    public static final ID3FrameType SYNCEDLYRICS = new ID3FrameType("SLT",
            "SYLT", "SYLT", "Synchronised Lyrics/Text");
    public static final ID3FrameType VOLUMEADJ = new ID3FrameType("RVA",
            "RVAD", null, "Relative Volume Adjustment");
    public static final ID3FrameType kVOLUMEADJ2 = new ID3FrameType(null, null,
            "RVA2", "Relative Volume Adjustment (2)");
    public static final ID3FrameType EQUALIZATION = new ID3FrameType("EQU",
            "EQUA", null, "Equalization");
    public static final ID3FrameType kEQUALIZATION2 = new ID3FrameType(null,
            null, "EQU2", "Equalization (2)");
    public static final ID3FrameType REVERB = new ID3FrameType("REV", "RVRB",
            "RVRB", "Reverb");
    public static final ID3FrameType PICTURE = new ID3FrameType("PIC", "APIC",
            "APIC", "Attached Picture");
    public static final ID3FrameType GENERALOBJECT = new ID3FrameType("GEO",
            "GEOB", "GEOB", "General Encapsulated Object");
    public static final ID3FrameType PLAYCOUNTER = new ID3FrameType("CNT",
            "PCNT", "PCNT", "Play Counter");
    public static final ID3FrameType POPULARIMETER = new ID3FrameType("POP",
            "POPM", "POPM", "Popularimeter");
    public static final ID3FrameType BUFFERSIZE = new ID3FrameType("BUF",
            "RBUF", "RBUF", "Recommended Buffer Size");
    public static final ID3FrameType CRYPTEDMETA = new ID3FrameType("CRM",
            null, null, "Encrypted Meta Frame");
    public static final ID3FrameType AUDIOCRYPTO = new ID3FrameType("CRA",
            "AENC", "AENC", "Audio Encryption");
    public static final ID3FrameType LINKEDINFO = new ID3FrameType("LNK",
            "LINK", "LINK", "Linked Information");
    public static final ID3FrameType POSITIONSYNC = new ID3FrameType(null,
            "POSS", "POSS", "Position Synchronisation Frame");
    public static final ID3FrameType COMMERCIAL = new ID3FrameType(null,
            "COMR", "COMR", "Commercial Frame");
    public static final ID3FrameType CRYPTOREG = new ID3FrameType(null, "ENCR",
            "ENCR", "Encryption Method Registration");
    public static final ID3FrameType GROUPINGREG = new ID3FrameType(null,
            "GRID", "GRID", "Group Indentification Registration");
    public static final ID3FrameType PRIVATE = new ID3FrameType(null, "PRIV",
            "PRIV", "Private Frame");
    public static final ID3FrameType OWNERSHIP = new ID3FrameType(null, "OWNE",
            "OWNE", "Ownership Frame");
    public static final ID3FrameType SIGNATURE = new ID3FrameType(null, null,
            "SIGN", "Signature Frame");
    public static final ID3FrameType SEEKFRAME = new ID3FrameType(null, null,
            "SEEK", "Seek Frame");
    public static final ID3FrameType AUDIOSEEKPOINT = new ID3FrameType(null,
            null, "ASPI", "Audio Seek Point Index");
    //

    public static final ID3FrameType kALL[] = {
            GENRE_ID, //
            CONTENT_GROUP, //
            TITLE, //
            SUBTITLE, //
            ARTIST, //
            BAND, //
            CONDUCTOR, //
            MIXARTIST, //
            COMPOSER, //
            LYRICIST, //
            LANGUAGE, //
            CONTENTTYPE, //
            ALBUM, //
            TRACKNUM, //
            PARTINSET, //
            ISRC, //
            DATE, //
            YEAR, //
            TIME, //
            RECORDINGDATES, //
            RECORDINGTIME, //
            ORIGYEAR, //
            ORIGRELEASETIME, //
            BPM, //
            MEDIATYPE, //
            FILETYPE, //
            COPYRIGHT, //
            PUBLISHER, //
            ENCODEDBY, //
            ENCODERSETTINGS, //
            SONGLEN, //
            SIZE, //
            PLAYLISTDELAY, //
            INITIALKEY, //
            ORIGALBUM, //
            ORIGFILENAME, //
            ORIGARTIST, //
            ORIGLYRICIST, //
            FILEOWNER, //
            NETRADIOSTATION, //
            NETRADIOOWNER, //
            SETSUBTITLE, //
            MOOD, //
            PRODUCEDNOTICE, //
            ENCODINGTIME, //
            RELEASETIME, //
            TAGGINGTIME, //
            ALBUMSORTORDER, //
            PERFORMERSORTORDER, //
            TITLESORTORDER, //
            USERTEXT, //
            WWWAUDIOFILE, //
            WWWARTIST, //
            WWWAUDIOSOURCE, //
            WWWCOMMERCIALINFO, //
            WWWCOPYRIGHT, //
            WWWPUBLISHER, //
            WWWRADIOPAGE, //
            WWWPAYMENT, //
            WWWUSER, //
            INVOLVEDPEOPLE, //
            MUSICIANCREDITLIST, //
            kINVOLVEDPEOPLE2, //
            UNSYNCEDLYRICS, //
            COMMENT, //
            TERMSOFUSE, //
            UNIQUEFILEID, //
            CDID, //
            EVENTTIMING, //
            MPEGLOOKUP, //
            SYNCEDTEMPO, //
            SYNCEDLYRICS, //
            VOLUMEADJ, //
            kVOLUMEADJ2, //
            EQUALIZATION, //
            kEQUALIZATION2, //
            REVERB, //
            PICTURE, //
            GENERALOBJECT, //
            PLAYCOUNTER, //
            POPULARIMETER, //
            BUFFERSIZE, //
            CRYPTEDMETA, //
            AUDIOCRYPTO, //
            LINKEDINFO, //
            POSITIONSYNC, //
            COMMERCIAL, //
            CRYPTOREG, //
            GROUPINGREG, //
            PRIVATE, //
            OWNERSHIP, //
            SIGNATURE, //
            SEEKFRAME, //
            AUDIOSEEKPOINT, //
    };

    private static final Map kMap = new Hashtable();

    public static ID3FrameType get(String id) {
        ID3FrameType result = (ID3FrameType) kMap.get(id);
        return result;
    }

    static {
        try {
            for (ID3FrameType aKALL : kALL) {
                if (aKALL.short_id != null)
                    kMap.put(aKALL.short_id, aKALL);
                if (aKALL.long_id != null)
                    kMap.put(aKALL.long_id, aKALL);
            }

        } catch (Throwable e) {

        }
    }

}
