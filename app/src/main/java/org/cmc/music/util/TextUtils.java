/*
 * Modified By Romulus U. Ts'ai
 * Copied from TextUtils.java in SharedLib (http://www.fightingquaker.com/sharedlib/)
 * On Oct 6, 2008
 *
 */

package org.cmc.music.util;

import java.util.Vector;

public class TextUtils implements BasicConstants {

    public static final String kALPHABET_NUMERALS = "0123456789";
    public static final String kALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    public static final String kALPHABET_UPPERCASE = kALPHABET_LOWERCASE
            .toUpperCase();
    public static final String kALPHABET = kALPHABET_LOWERCASE
            + kALPHABET_UPPERCASE;
    public static final String kFILENAME_SAFE = kALPHABET + kALPHABET_NUMERALS
            + " ._-()&,[]";

    public static String filter(String s, String filter) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (filter.indexOf(c) >= 0)
                result.append(c);
        }

        return result.toString();
    }

    public static String[] split(String s, String token) {
        //		if (s == null)
        //			return s;
        //
        Vector result = new Vector();

        int index;
        while ((index = s.indexOf(token)) >= 0) {
            result.add(s.substring(0, index));
            s = s.substring(index + token.length());
        }
        result.add(s);

        String splits[] = new String[result.size()];
        for (int i = 0; i < result.size(); i++)
            splits[i] = (String) result.get(i);
        return splits;
    }

    public static String replace(String s, String find, String replaceto) {
        if (s == null)
            return s;

        StringBuilder result = new StringBuilder();

        int index;
        while ((index = s.indexOf(find)) >= 0) {
            result.append(s.substring(0, index));
            result.append(replaceto);
            s = s.substring(index + find.length());
        }
        result.append(s);

        return result.toString();
    }

    public static final String join(String splits[], String token) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < splits.length; i++) {
            if (i > 0)
                result.append(token);

            result.append(splits[i]);
        }

        return result.toString();
    }
}