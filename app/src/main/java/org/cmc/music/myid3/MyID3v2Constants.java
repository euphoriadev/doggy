package org.cmc.music.myid3;

import org.cmc.music.metadata.MusicMetadataConstants;

public interface MyID3v2Constants extends MusicMetadataConstants {
    public static final int HEADER_FLAG_ID3v22_UNSYNCHRONISATION = 1 << 7;
    public static final int HEADER_FLAG_ID3v22_COMPRESSION = 1 << 6;

    public static final int HEADER_FLAG_ID3v23_UNSYNCHRONISATION = 1 << 7;
    public static final int HEADER_FLAG_ID3v23_EXTENDED_HEADER = 1 << 6;
    public static final int HEADER_FLAG_ID3v23_EXPERIMENTAL_INDICATOR = 1 << 5;

    public static final int HEADER_FLAG_ID3v24_UNSYNCHRONISATION = 1 << 7;
    public static final int HEADER_FLAG_ID3v24_EXTENDED_HEADER = 1 << 6;
    public static final int HEADER_FLAG_ID3v24_EXPERIMENTAL_INDICATOR = 1 << 5;
    public static final int HEADER_FLAG_ID3v24_FOOTER_PRESENT = 1 << 4;

    public static final int FRAME_FLAG_ID3v24_TAG_ALTER_PRESERVATION = 1 << 14;
    public static final int FRAME_FLAG_ID3v24_FILE_ALTER_PRESERVATION = 1 << 13;
    public static final int FRAME_FLAG_ID3v24_READ_ONLY = 1 << 12;
    public static final int FRAME_FLAG_ID3v24_GROUPING_IDENTITY = 1 << 6;
    public static final int FRAME_FLAG_ID3v24_COMPRESSION = 1 << 3;
    public static final int FRAME_FLAG_ID3v24_ENCRYPTION = 1 << 2;
    public static final int FRAME_FLAG_ID3v24_UNSYNCHRONISATION = 1 << 1;
    public static final int FRAME_FLAG_ID3v24_DATA_LENGTH_INDICATOR = 1 << 0;

    public static final int FRAME_FLAG_ID3v23_TAG_ALTER_PRESERVATION = 1 << 15;
    public static final int FRAME_FLAG_ID3v23_FILE_ALTER_PRESERVATION = 1 << 14;
    public static final int FRAME_FLAG_ID3v23_READ_ONLY = 1 << 13;
    public static final int FRAME_FLAG_ID3v23_COMPRESSION = 1 << 7;
    public static final int FRAME_FLAG_ID3v23_ENCRYPTION = 1 << 6;
    public static final int FRAME_FLAG_ID3v23_GROUPING_IDENTITY = 1 << 5;

    public static final String CHAR_ENCODING_ISO = "ISO-8859-1";
    public static final String CHAR_ENCODING_UTF_8 = "UTF-8";
    public static final String CHAR_ENCODING_UTF_16 = "UTF-16";
    public static final String CHAR_ENCODING_UTF_16_WITH_BOM = "UTF-16 with BOM";
    public static final String CHAR_ENCODING_UTF_16_WITHOUT_BOM = "UTF-16 without BOM";

    public static final int CHAR_ENCODING_CODE_ISO_8859_1 = 0;
    public static final int CHAR_ENCODING_CODE_UTF_16_WITH_BOM = 1;
    public static final int CHAR_ENCODING_CODE_UTF_16_NO_BOM = 2;
    public static final int CHAR_ENCODING_CODE_UTF_8 = 3;

    public static final int FRAME_HEADER_LENGTH = 10;
    public final static int TAG_HEADER_LENGTH = 10;

    public static final int PICTURE_TYPE_OTHER = 0x00;
    public static final int PICTURE_TYPE_32X32_PIXELS_FILE_ICON_PNG_ONLY = 0x01;
    public static final int PICTURE_TYPE_OTHER_FILE_ICON = 0x02;
    public static final int PICTURE_TYPE_COVER_FRONT = 0x03;
    public static final int PICTURE_TYPE_COVER_BACK = 0x04;
    public static final int PICTURE_TYPE_LEAFLET_PAGE = 0x05;
    public static final int PICTURE_TYPE_MEDIA_EG_LABEL_SIDE_OF_CD = 0x06;
    public static final int PICTURE_TYPE_LEAD_ARTIST_LEAD_PERFORMER_SOLOIST = 0x07;
    public static final int PICTURE_TYPE_ARTIST_PERFORMER = 0x08;
    public static final int PICTURE_TYPE_CONDUCTOR = 0x09;
    public static final int PICTURE_TYPE_BAND_ORCHESTRA = 0x0A;
    public static final int PICTURE_TYPE_COMPOSER = 0x0B;
    public static final int PICTURE_TYPE_LYRICIST_TEXT_WRITER = 0x0C;
    public static final int PICTURE_TYPE_RECORDING_LOCATION = 0x0D;
    public static final int PICTURE_TYPE_DURING_RECORDING = 0x0E;
    public static final int PICTURE_TYPE_DURING_PERFORMANCE = 0x0F;
    public static final int PICTURE_TYPE_MOVIE_VIDEO_SCREEN_CAPTURE = 0x10;
    public static final int PICTURE_TYPE_A_BRIGHT_COLOURED_FISH = 0x11;
    public static final int PICTURE_TYPE_ILLUSTRATION = 0x12;
    public static final int PICTURE_TYPE_BAND_ARTIST_LOGOTYPE = 0x13;
    public static final int PICTURE_TYPE_PUBLISHER_STUDIO_LOGOTYPE = 0x14;

}
