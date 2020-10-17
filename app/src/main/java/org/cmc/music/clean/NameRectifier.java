/*
 * Written By Charles M. Chen
 *
 * Created on Jan 1, 2006
 *
 */

package org.cmc.music.clean;

import org.cmc.music.metadata.MusicMetadataConstants;
import org.cmc.music.util.MyComparator;
import org.cmc.music.util.MyMap;
import org.cmc.music.util.TextUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class NameRectifier implements MusicMetadataConstants {

    private static final String DEFAULTS[] = {
            "album", //
            "artist", //
            "title", //
            "no title", //
            "no artist", //
            "undefined", //
            "va", //
            "mp3", //
            "cd", //
            "genre", //
            "unknown", //
            "name", //
            "n/a", //
            "Untitled", //
    };

    static {
        Arrays.sort(DEFAULTS, MyComparator.kToStringLengthReverse);
    }

    private static final String DEFAULTS_VA[] = {
            "Compilation", //
            "V.A", //
            "V.A.", //
            "V. A.", //
            "V. A", //
            "V/A", //
            "Va", //
            "V A", //
            "Various Artists", //
            "Various", //
            "Varioius", //
            "Varied Artists", //
            "Varias", //
            "Varios Interpretes", //
            "Varios", //
            "Various Artist", //
            "Various Artistses", //
            "Various Artits", //
            "Various Artisis", //
            "Various Aritsts", //
            "Varius Artists", //
            "Various Composers", //
            "Various djs", //
    };

    static {
        Arrays.sort(DEFAULTS_VA, MyComparator.kToStringLengthReverse);
    }

    private static final String DEFAULTS_SOUNDTRACK[] = {
            "The Motion Picture".toLowerCase(), //
            "Motion Picture".toLowerCase(), //
            "Original Motion Picture".toLowerCase(), //
            "Original Motion Picture Soundtrack".toLowerCase(), //
            "The Soundtrack".toLowerCase(), //
            "Music From The Motion Picture".toLowerCase(), //
            "Original Soundtrack Recording".toLowerCase(), //
            "Trilha Sonora Original".toLowerCase(), //
            "ost", //
            "original soundtrack", //
            "soundtrack", //
            "Music From The Motion Picture Soundtrack", //
    };

    static {
        Arrays.sort(DEFAULTS_SOUNDTRACK, MyComparator.kToStringLengthReverse);
    }

    private static final String DEFAULT_ACAPELLA = "Ac+ap+el+as?".toLowerCase();

    public String rectifyGeneric(String s) {
        return rectifyGeneric(s, null);
    }

    public String rectifyGeneric(String s, Map flags) {
        String old = s;
        while (true) {
            s = rectifyGeneric_1(s, flags);
            s = removeQuotes(s);
            if (s == null)
                return null;
            if (s.equals(old))
                return s;
            old = s;
        }
    }

    private String removeQuotes(String s) {
        if (s == null)
            return null;
        if (s.matches("^\".+\"$") || s.matches("^'.+'$")
                || s.matches("^\\{.+}$")
                || s.matches("^\\(.+\\)$")
                || s.matches("^\\[.+]$")
                || s.matches("^<.+>$")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static final String ROMAN_NUMERALS = "ivx";

    private boolean isRomanNumeral(String s) {
        char chars[] = s.toCharArray();
        for (char c : chars) {
            if (ROMAN_NUMERALS.indexOf(c) < 0
                    && ROMAN_NUMERALS.toUpperCase().indexOf(c) < 0)
                return false;
        }
        return true;
    }

    private static final Map natural_numbers = new MyMap();

    static {
        natural_numbers.put("zero", 0);
        natural_numbers.put("one", 1);
        natural_numbers.put("two", 2);
        natural_numbers.put("three", 3);
        natural_numbers.put("four", 4);
        natural_numbers.put("five", 5);
        natural_numbers.put("six", 6);
        natural_numbers.put("seven", 7);
        natural_numbers.put("eight", 8);
        natural_numbers.put("nine", 9);
        natural_numbers.put("ten", 10);
        natural_numbers.put("eleven", 11);
        natural_numbers.put("twelve", 12);
        natural_numbers.put("thirteen", 13);
        natural_numbers.put("fourteen", 14);
        natural_numbers.put("fifteen", 15);
        natural_numbers.put("sixteen", 16);
        natural_numbers.put("seventeen", 17);
        natural_numbers.put("eighteen", 18);
        natural_numbers.put("nineteen", 19);
        natural_numbers.put("twenty", 20);
    }

    private Number parseNumber(String s) {
        if (s == null)
            return null;
        s = s.trim();
        if (s.length() < 1)
            return null;

        try {
            return Integer.valueOf(s.trim());
        } catch (Throwable e) {

        }
        return (Number) natural_numbers.get(s.toLowerCase());
    }

    private String clean(String s, Map flags) {
        s = s.trim();
        s = Diacriticals.convertDiacriticals(s);

        while (s.startsWith("-"))
            s = s.substring(1);

        s = removeSafePrefixSuffix(s, DEFAULTS);
        if (s == null)
            return null;

        String suffixes[] = {
                " ", //
                "-", //
                ".Mp3", //
                " Mp3", //
        };
        s = removeSuffixes(s, suffixes);
        String prefixes[] = {
                " ", //
                "-", //
        };
        s = removePrefixes(s, prefixes);

        s = s.replace('_', ' ');
        s = TextUtils.replace(s, "-", " - ");
        s = TextUtils.replace(s, "`", "'");
        s = TextUtils.replace(s, "�", "'");
        s = TextUtils.replace(s, "�", "'");
        s = TextUtils.replace(s, "[", "(");
        s = TextUtils.replace(s, "]", ")");
        s = TextUtils.replace(s, "(", " (");
        s = TextUtils.replace(s, "~", "-");
        s = TextUtils.replace(s, "  ", " ");
        s = TextUtils.replace(s, "  ", " ");
        s = TextUtils.replace(s, "..", ".");
        s = TextUtils.replace(s, "--", "-");
        s = TextUtils.replace(s, "- -", "-");

        s = TextUtils.replace(s, "#", "No. ");

        {
            String old = s;


            s = removeSafePrefixSuffix(s, DEFAULTS_SOUNDTRACK, true);


            if (s == null || !s.equals(old)) {
                if (flags != null) {
                    flags.put(KEY_SOUNDTRACK, Boolean.TRUE);
                    //					flags.put(KEY_COMPILATION, Boolean.TRUE);
                }
            }
            if (s == null)
                return null;
        }

        {
            String temp = removeSafePrefixSuffix(s, DEFAULT_ACAPELLA, true);
            //			String temp = removeSafePrefixSuffix(s, kDEFAULTS_acapella);


            if (temp == null || !s.equals(temp)) {
                if (flags != null)
                    flags.put(KEY_ACAPELLA, Boolean.TRUE);
            }
        }


        {
            String old = s;

            s = removeSafePrefixSuffix(s, DEFAULTS_VA, true);

            if (s == null || !s.equals(old)) {
                if (flags != null)
                    flags.put(KEY_COMPILATION, Boolean.TRUE);
            }
            if (s == null)
                return null;
        }

        {
            String splits[] = TextUtils.split(s, " ");
            for (int i = 0; i < splits.length; i++) {
                if (isRomanNumeral(splits[i]))
                    splits[i] = splits[i].toUpperCase();
            }
            s = TextUtils.join(splits, " ");
        }

        s = s.trim();


        //		if (s == null)
        //			return null;

        //		s = insertSpacesBeforeCaps(s);
        s = toTitleCase(s);

        return s;
    }

    private String toTitleCase(String s) {
        StringBuilder result = new StringBuilder();
        char prev = 0;


        char chars[] = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isLetter(c)) {
                if (i == 0)
                    result.append(Character.toUpperCase(c));
                else if ((prev == '\''))
                    //					else if ((prev == '\'') && Character.isLetter(next))
                    result.append(Character.toLowerCase(c));
                else if (!Character.isLetter(prev))
                    result.append(Character.toUpperCase(c));
                else
                    result.append(Character.toLowerCase(c));
            } else
                result.append(c);
            prev = c;
        }


        return result.toString();
    }

    private String insertSpacesBeforeCaps(String s) {
        StringBuilder result = new StringBuilder();

        char prev = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isLetter(c) && (i > 0) && Character.isLetter(prev)
                    && Character.isLowerCase(prev) && Character.isUpperCase(c))
                result.append(' ');

            result.append(c);

            prev = c;
        }

        return result.toString();
    }

    private String rectifyGeneric_1(String s, Map flags) {


        if (s == null)
            return null;

        s = s.trim();
        if (s.length() < 1)

            return null;
        //		s = new MusicOrganizerFilter().getNewName2(s);
        s = clean(s, flags);


        if (s == null)
            return null;

        s = TextUtils.replace(s, ".", ". ");
        s = TextUtils.replace(s, " .", " ");
        s = TextUtils.replace(s, "  ", " ");


        if (s.matches("^\\?+$")) {
            return null;
        }


        while (s.startsWith("."))
            s = s.substring(1);

        s = TextUtils.replace(s, "Live @ ", "Live At ");
        s = TextUtils.replace(s, "Live@", "Live At ");

        if (s == null)
            return null;

        if (s.endsWith(", The"))
            s = "The " + s.substring(0, s.length() - 5);

        return s;
    }

    public String rectifySongTitle(
            //			Artist artist, Album album,
            String s, Map flags) {
        String old = s;
        //		while (true)
        for (int i = 0; true; i++) {


            s = rectifySongTitle_1(s, flags);
            if (s == null)
                return null;

            if (s.equals(old))
                return s;
            old = s;
        }
    }

    private void parseTrackNumber(String s, Map flags) {
        if (flags == null)
            return;

        try {
            s = s.trim();
            Number number = Integer.valueOf(s);
            if (flags != null)
                flags.put(KEY_TRACK_NUMBER, number);

        } catch (Throwable e) {

        }
    }

    private String removeTrackNumbers(String s, Map flags) {
        if (s == null)
            return null;

        if (s.toLowerCase().matches("^(audio)? ?track ?[- ]?[0-9][0-9]?$")) {
            if (s.toLowerCase().startsWith("audio"))
                s = s.substring(5).trim();
            parseTrackNumber(s.substring(5), flags);

            return null;
        }
        if (s.toLowerCase().matches("^piste ?[- ]?[0-9][0-9]?$")) {
            parseTrackNumber(s.substring(5), flags);

            return null;
        }


        if (s.matches("^[0-9][0-9] - ")
                || s.matches("^[0-9][0-9][0-9] - ")
                || s.matches("^[aAbBcCdD][0-9] - ")) {

            int index = s.indexOf('-');
            if (index >= 0) {
                String after = s.substring(index + 1).trim();

                if (after.indexOf('-') < 0) // if mutiple -'s then ignore...
                {
                    parseTrackNumber(s.substring(0, index), flags);
                    s = after;
                }
            }
        }

        if (s.matches("^\\([0-9][0-9]\\) ")
                || s.matches("^\\([abcdABCD][0-9]\\) ")) {

            int index = s.indexOf(')');
            if (index >= 0) {
                parseTrackNumber(s.substring(1, index), flags);
                s = s.substring(index + 1).trim();
            }
        }

        return s;
    }

    private String rectifySongTitle_1(String s, Map flags) {
        s = rectifyGeneric_1(s, flags);
        if (s == null)
            return null;

        s = removeTrackNumbers(s, flags);

        if (s == null)
            return null;

        s = removeQuotes(s);

        return s;
    }

    private String removeSuffixes(String s, String suffixes[]) {
        return removeSuffixes(s, new Vector(Arrays.asList(suffixes)));
    }

    private String removeSuffixes(String s, Vector suffixes) {
        if (s == null)
            return null;

        for (int i = 0; i < suffixes.size(); i++) {
            String suffix = (String) suffixes.get(i);

            if (s.toLowerCase().endsWith(suffix.toLowerCase()))
                s = s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    private String removePrefixes(String s, String prefixes[]) {
        return removePrefixes(s, new Vector(Arrays.asList(prefixes)));
    }

    private String removePrefixes(String s, Vector prefixes) {
        if (s == null)
            return null;

        for (int i = 0; i < prefixes.size(); i++) {
            String prefix = (String) prefixes.get(i);

            if (s.toLowerCase().startsWith(prefix.toLowerCase()))
                s = s.substring(prefix.length());
        }
        return s;
    }

    public String rectifyAlbum(String s) {
        return rectifyAlbum(
                //				null,
                s, null);
    }

    public String rectifyAlbum(
            //			Artist artist,
            String s, Map flags) {
        String old = s;
        while (true) {
            s = rectifyAlbum_1(s, flags);
            if (s == null)
                return null;

            if (s.equals(old))
                return s;
            old = s;
        }
    }

    private String removeYearPrefixSuffix(String s) {


        if (s == null)
            return null;

        if (s.matches("^\\(199[0-9]\\)")
                || s.matches("^\\(200[0-9]\\)"))
            s = s.substring(7);

        if (s.matches("^\\( 199[0-9] \\)")
                || s.matches("^\\( 200[0-9] \\)"))
            s = s.substring(9);

        if (s.matches("\\(199[0-9]\\)$")
                || s.matches("\\(200[0-9]\\)$"))
            s = s.substring(0, s.length() - 7);

        if (s.matches("\\( 199[0-9] \\)$")
                || s.matches("\\( 200[0-9] \\)$"))
            s = s.substring(0, s.length() - 9);

        if (s.matches("199[0-9] - ") || s.matches("200[0-9] - ")) {
            int index = s.indexOf('-');
            if (index >= 0) {
                String temp = s.substring(index + 1);
                if (temp.indexOf('-') < 0)
                    s = temp;
            }
        }

        if (s.matches("- 199[0-9]") || s.matches(" - 200[0-9]")) {
            int index = s.lastIndexOf('-');
            if (index >= 0) {
                String temp = s.substring(0, index);
                if (temp.indexOf('-') < 0)
                    s = temp;
            }
        }


        return s;
    }

    private static final String PATTERNS_ALBUM[] = {
            "dvd", //
            "10\"", //
            "12 - Inch", //
            "12 Inch", //
            "12 Inch Single", //
            "12\"", //
            "12\" Ep", //
            "12\" Vinyl", //
            "7 Inch", //
            "7\"", //
            "Advance", //
            "Advance Copy", //
            "Bonus Disc", //
            "Box", //
            "Cd", //
            "Cd Single", //
            "Cdm", //
            "Cdr", //
            "Cds", //
            "maxi", //
            "maxi single", //
            "Promo Cd", //
            "Ep", //
            "Full Vls", //
            //			"Vls", //
            "Import", //
            "Lp", //
            //			"Ost", //
            "Promo", //
            "Promo Cds", //
            "Retail", //
            "Single", //
            "Vinyl", //
            "Vinyl Single", //
            "Vls", //
            "cd", //
            "cds", //
            "ep", //
            "unknown album", //
            "Remastered", //
    };

    static {
        Arrays.sort(PATTERNS_ALBUM, MyComparator.kToStringLengthReverse);
    }

    private static final String PATTERNS_ARTIST[] = {
            "skit", //
            "live", //
    };

    static {
        Arrays.sort(PATTERNS_ARTIST, MyComparator.kToStringLengthReverse);
    }

    public String rectifyAlbum_1(String s, Map flags) {
        s = rectifyGeneric_1(s, flags);
        if (s == null)
            return null;

        s = removeSafePrefixSuffix(s, PATTERNS_ALBUM);
        if (s == null)
            return null;

        if (s.endsWith(" Box Set")) {
            if (flags != null)
                flags.put(KEY_COMPILATION, Boolean.TRUE);
        }

        s = removeYearPrefixSuffix(s);

        s = removeURLs(s);

        s = removeQuotes(s);

        {
            String old = s;

            s = removeSafePrefixSuffix(s, DEFAULT_ACAPELLA, true);
            //			s = removeSafePrefixSuffix(s, kDEFAULTS_acapella);

            if (s == null || !s.equals(old)) {
                if (flags != null) {
                    flags.put(KEY_ACAPELLA, Boolean.TRUE);
                    //					flags.put(KEY_COMPILATION, Boolean.TRUE);
                }
            }
            if (s == null)
                return null;
        }

        if (s.endsWith(" !"))
            s = s.substring(0, s.length() - 2);
        else if (s.endsWith(" (!)"))
            s = s.substring(0, s.length() - 4);

        return s;
    }

    private String removeURLs(String s) {
        if (s == null)
            return null;

        {
            if (s.toLowerCase().matches("^http://"))
                return null;
        }

        {
            String temp = s;
            temp = TextUtils.replace(temp, ". ", ".");

            if (temp.toLowerCase().matches("^[\\w \\-]*\\.[\\w .\\-]*\\.(com|net|org|edu)$"))
                return null;

        }

        return s;
    }

    public String rectifyArtist(String s) {
        return rectifyArtist(s, null);
    }

    public String rectifyArtist(String s, Map flags) {
        String old = s;
        while (true) {
            s = rectifyArtist_1(s, flags);
            if (s == null)
                return null;

            //			s = removeTrackNumbers(s);

            if (s.equals(old))
                return s;
            old = s;
        }
    }

    private String rectifyArtist_1(String s, Map flags) {


        s = rectifyGeneric_1(s, flags);
        if (s == null)
            return null;


        if (s.equalsIgnoreCase("unknown artist"))
            return null;


        s = removeTrackNumbers(s, flags);
        s = removeYearPrefixSuffix(s);

        s = removeSafePrefixSuffix(s, PATTERNS_ARTIST);
        if (s == null)
            return null;

        {
            String old = s;

            //			s = removeSafePrefixSuffix(s, kDEFAULTS_acapella);
            s = removeSafePrefixSuffix(s, DEFAULT_ACAPELLA, true);

            if (s == null || !s.equals(old)) {
                if (flags != null) {
                    flags.put(KEY_ACAPELLA, Boolean.TRUE);
                    //					flags.put(KEY_COMPILATION, Boolean.TRUE);
                }
            }
            if (s == null)
                return null;
        }

        s = removeQuotes(s);
        s = removeURLs(s);

        return s;
    }

    public String rectifyGenre(String s) {
        String old = s;
        while (true) {
            s = rectifyGenre_1(s);
            if (s == null)
                return null;
            if (s.equals(old))
                return s;
            old = s;
        }
    }

    private String rectifyGenre_1(String s) {
        s = rectifyGeneric_1(s, null);
        if (s == null)
            return null;

        if (s.equalsIgnoreCase("music"))
            return null;

        s = removeQuotes(s);

        s = TextUtils.replace(s, " - ", "-");

        s = removeSafePrefixSuffix(s, "�", true);
        s = removeSafePrefixSuffix(s, DEFAULT_ACAPELLA, true);

        return s;
    }

    private String rectifyPublisher_1(String s) {
        s = rectifyGeneric_1(s, null);
        if (s == null)
            return null;

        s = removeURLs(s);
        s = removeQuotes(s);
        s = TextUtils.replace(s, " - ", "-");

        return s;
    }

    private static final String FEATURING[] = {
            "f\\.", //
            "ft\\.", //
            "feat\\.", //
            "featuring ", //
    };

    private static final String ESCAPED = "^$.[|*+?\\(<)>#=/-{}";

    public String toRegexLiteral(String s) {
        StringBuilder result = new StringBuilder();

        char chars[] = s.toCharArray();
        for (char c : chars) {
            if (ESCAPED.indexOf(c) >= 0)
                result.append('\\');

            result.append(c);
        }
        return result.toString();
    }


    public String getPrefixPattern(String s, boolean permissive) {
        return "^('" + s + "'|\\\"" + s + "\\\"|\\[" + s + "\\]|\\(" + s
                + "\\)|\\{" + s + "\\}|" + s + "\\-"
                + (permissive ? "|" + s + " " : "") + ")";
    }

    public String getSuffixPattern(String s, boolean permissive) {
        return "('" + s + "'|\\\"" + s + "\\\"|\\[" + s + "\\]|\\(" + s
                + "\\)|\\{" + s + "\\}|\\-" + s + ""
                + (permissive ? "| " + s : "") + ")$";
    }

    public String getPrefixPattern2(String s) {
        return "^('.*'|\\\".*\\\"|\\[.*\\]|\\(.*\\)|\\{.*\\}|.*\\-) ?" + s
                + "$";
    }

    public String getSuffixPattern2(String s) {
        return "^" + s
                + " ?('.*'|\\\".*\\\"|\\[.*\\]|\\(.*\\)|\\{.*\\}|\\-.*)$";
    }

    private String stripRegexMatch(String s, String pattern) {
        if (s == null)
            return null;

        try {

            if (!s.toLowerCase().matches(pattern))
                return s;

            return s;
        } catch (Exception e) {
            return s;
        }
    }

    private String extractRegexPattern(String s, String pattern, int paren) {
        if (s == null)
            return null;

        try {
            Pattern ptr = Pattern.compile(pattern);
            if (!ptr.matcher(s.toLowerCase()).matches())
                return s;

            return s;
        } catch (Exception e) {
            return s;
        }
    }

    private String removeSafePrefixSuffix(String s, String patterns[]) {
        return removeSafePrefixSuffix(s, patterns, false);
    }

    private String removeSafePrefixSuffix(String s, String patterns[],
                                          boolean permissive) {
        if (s == null)
            return null;

        for (int i = 0; s != null && i < patterns.length; i++) {
            String pattern = patterns[i];

            s = removeSafePrefixSuffixLiteral(s, pattern, permissive);
        }

        return s;
    }

    private String removeSafePrefixSuffixLiteral(String s, String pattern,
                                                 boolean permissive) {
        return removeSafePrefixSuffix(s, toRegexLiteral(pattern), permissive);
    }

    private String removeSafePrefixSuffix(String s, String pattern,
                                          boolean permissive) {
        if (s == null)
            return null;

        if (s.equalsIgnoreCase(pattern))
            return null;

        s = stripRegexMatch(s, getPrefixPattern((pattern), permissive));
        s = stripRegexMatch(s, getSuffixPattern((pattern), permissive));

        s = extractRegexPattern(s, getPrefixPattern2((pattern)), 1);
        s = extractRegexPattern(s, getSuffixPattern2((pattern)), 1);

        return s;
    }

}
