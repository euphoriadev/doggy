/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 *
 * Removed all Debug executions
 *
 */

package org.cmc.music.common;

import org.cmc.music.util.MyMap;

import java.util.Map;

public class ID3v1Genre {
    public final int id;
    public final String name;

    public ID3v1Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private static final ID3v1Genre ALL[] = {
            new ID3v1Genre(0, "Blues"), //
            new ID3v1Genre(1, "Classic Rock"), //
            new ID3v1Genre(2, "Country"), //
            new ID3v1Genre(3, "Dance"), //
            new ID3v1Genre(4, "Disco"), //
            new ID3v1Genre(5, "Funk"), //
            new ID3v1Genre(6, "Grunge"), //
            new ID3v1Genre(7, "Hip-Hop"), //
            new ID3v1Genre(8, "Jazz"), //
            new ID3v1Genre(9, "Metal"), //
            new ID3v1Genre(10, "New Age"), //
            new ID3v1Genre(11, "Oldies"), //
            new ID3v1Genre(12, "Other"), //
            new ID3v1Genre(13, "Pop"), //
            new ID3v1Genre(14, "R&B"), //
            new ID3v1Genre(15, "Rap"), //
            new ID3v1Genre(16, "Reggae"), //
            new ID3v1Genre(17, "Rock"), //
            new ID3v1Genre(18, "Techno"), //
            new ID3v1Genre(19, "Industrial"), //
            new ID3v1Genre(20, "Alternative"), //
            new ID3v1Genre(21, "Ska"), //
            new ID3v1Genre(22, "Death Metal"), //
            new ID3v1Genre(23, "Pranks"), //
            new ID3v1Genre(24, "Soundtrack"), //
            new ID3v1Genre(25, "Euro-Techno"), //
            new ID3v1Genre(26, "Ambient"), //
            new ID3v1Genre(27, "Trip-Hop"), //
            new ID3v1Genre(28, "Vocal"), //
            new ID3v1Genre(29, "Jazz+Funk"), //
            new ID3v1Genre(30, "Fusion"), //
            new ID3v1Genre(31, "Trance"), //
            new ID3v1Genre(32, "Classical"), //
            new ID3v1Genre(33, "Instrumental"), //
            new ID3v1Genre(34, "Acid"), //
            new ID3v1Genre(35, "House"), //
            new ID3v1Genre(36, "Game"), //
            new ID3v1Genre(37, "Sound Clip"), //
            new ID3v1Genre(38, "Gospel"), //
            new ID3v1Genre(39, "Noise"), //
            new ID3v1Genre(40, "AlternRock"), //
            new ID3v1Genre(41, "Bass"), //
            new ID3v1Genre(42, "Soul"), //
            new ID3v1Genre(43, "Punk"), //
            new ID3v1Genre(44, "Space"), //
            new ID3v1Genre(45, "Meditative"), //
            new ID3v1Genre(46, "Instrumental Pop"), //
            new ID3v1Genre(47, "Instrumental Rock"), //
            new ID3v1Genre(48, "Ethnic"), //
            new ID3v1Genre(49, "Gothic"), //
            new ID3v1Genre(50, "Darkwave"), //
            new ID3v1Genre(51, "Techno-Industrial"), //
            new ID3v1Genre(52, "Electronic"), //
            new ID3v1Genre(53, "Pop-Folk"), //
            new ID3v1Genre(54, "Eurodance"), //
            new ID3v1Genre(55, "Dream"), //
            new ID3v1Genre(56, "Southern Rock"), //
            new ID3v1Genre(57, "Comedy"), //
            new ID3v1Genre(58, "Cult"), //
            new ID3v1Genre(59, "Gangsta"), //
            new ID3v1Genre(60, "Top 40"), //
            new ID3v1Genre(61, "Christian Rap"), //
            new ID3v1Genre(62, "Pop/Funk"), //
            new ID3v1Genre(63, "Jungle"), //
            new ID3v1Genre(64, "Native American"), //
            new ID3v1Genre(65, "Cabaret"), //
            new ID3v1Genre(66, "New Wave"), //
            new ID3v1Genre(67, "Psychadelic"), //
            new ID3v1Genre(68, "Rave"), //
            new ID3v1Genre(69, "Showtunes"), //
            new ID3v1Genre(70, "Trailer"), //
            new ID3v1Genre(71, "Lo-Fi"), //
            new ID3v1Genre(72, "Tribal"), //
            new ID3v1Genre(73, "Acid Punk"), //
            new ID3v1Genre(74, "Acid Jazz"), //
            new ID3v1Genre(75, "Polka"), //
            new ID3v1Genre(76, "Retro"), //
            new ID3v1Genre(77, "Musical"), //
            new ID3v1Genre(78, "Rock & Roll"), //
            new ID3v1Genre(79, "Hard Rock"), //
            new ID3v1Genre(80, "Folk"), //
            new ID3v1Genre(81, "Folk-Rock"), //
            new ID3v1Genre(82, "National Folk"), //
            new ID3v1Genre(83, "Swing"), //
            new ID3v1Genre(84, "Fast Fusion"), //
            new ID3v1Genre(85, "Bebob"), //
            new ID3v1Genre(86, "Latin"), //
            new ID3v1Genre(87, "Revival"), //
            new ID3v1Genre(88, "Celtic"), //
            new ID3v1Genre(89, "Bluegrass"), //
            new ID3v1Genre(90, "Avantgarde"), //
            new ID3v1Genre(91, "Gothic Rock"), //
            new ID3v1Genre(92, "Progressive Rock"), //
            new ID3v1Genre(93, "Psychedelic Rock"), //
            new ID3v1Genre(94, "Symphonic Rock"), //
            new ID3v1Genre(95, "Slow Rock"), //
            new ID3v1Genre(96, "Big Band"), //
            new ID3v1Genre(97, "Chorus"), //
            new ID3v1Genre(98, "Easy Listening"), //
            new ID3v1Genre(99, "Acoustic"), //
            new ID3v1Genre(100, "Humour"), //
            new ID3v1Genre(101, "Speech"), //
            new ID3v1Genre(102, "Chanson"), //
            new ID3v1Genre(103, "Opera"), //
            new ID3v1Genre(104, "Chamber Music"), //
            new ID3v1Genre(105, "Sonata"), //
            new ID3v1Genre(106, "Symphony"), //
            new ID3v1Genre(107, "Booty Bass"), //
            new ID3v1Genre(108, "Primus"), //
            new ID3v1Genre(109, "Porn Groove"), //
            new ID3v1Genre(110, "Satire"), //
            new ID3v1Genre(111, "Slow Jam"), //
            new ID3v1Genre(112, "Club"), //
            new ID3v1Genre(113, "Tango"), //
            new ID3v1Genre(114, "Samba"), //
            new ID3v1Genre(115, "Folklore"), //
            new ID3v1Genre(116, "Ballad"), //
            new ID3v1Genre(117, "Power Ballad"), //
            new ID3v1Genre(118, "Rhythmic Soul"), //
            new ID3v1Genre(119, "Freestyle"), //
            new ID3v1Genre(120, "Duet"), //
            new ID3v1Genre(121, "Punk Rock"), //
            new ID3v1Genre(122, "Drum Solo"), //
            new ID3v1Genre(123, "A capella"), //
            new ID3v1Genre(124, "Euro-House"), //
            new ID3v1Genre(125, "Dance Hall"), //
    };

    private static String simplify(String s) {
        StringBuilder result = new StringBuilder(s.length());

        char chars[] = s.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c))
                result.append(Character.toLowerCase(c));
            else if (Character.isDigit(c))
                result.append(c);
        }

        return result.toString();
    }

    private static final Map ID_TO_NAME_MAP = new MyMap();
    private static final Map NAME_TO_ID_MAP = new MyMap();
    private static final Map SIMPLE_NAME_TO_ID_MAP = new MyMap();

    static {
        for (ID3v1Genre genre : ALL) {
            String name = genre.name;
            Number id = genre.id;

            ID_TO_NAME_MAP.put(id, name);
            NAME_TO_ID_MAP.put(name, id);

            String simple = simplify(name);
            SIMPLE_NAME_TO_ID_MAP.put(simple, id);
        }
    }

    public static Number get(String name) {
        Number result = (Number) NAME_TO_ID_MAP.get(name);
        if (result != null)
            return result;

        String simple = simplify(name);

        result = (Number) SIMPLE_NAME_TO_ID_MAP.get(simple);
        return result;
    }

    public static final String get(Number id) {
        return (String) ID_TO_NAME_MAP.get(id);
    }
}
