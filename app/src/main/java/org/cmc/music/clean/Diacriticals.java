/*
 * Written By Charles M. Chen
 *
 * Created on Jan 1, 2006
 *
 */

package org.cmc.music.clean;

import org.cmc.music.metadata.MusicMetadataConstants;

import java.util.HashMap;
import java.util.Map;

public abstract class Diacriticals implements MusicMetadataConstants {

    private static final Map<String, String> DIACRITICALS = new HashMap<>();

    static {
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "ae");
        DIACRITICALS.put("�", "c");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "n");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "y");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "a");
        DIACRITICALS.put("�", "ae");
        DIACRITICALS.put("�", "c");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "e");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "i");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "n");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "o");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
        DIACRITICALS.put("�", "u");
    }

    public static String convertDiacriticals(String s) {
        StringBuilder result = new StringBuilder();

        char chars[] = s.toCharArray();
        for (char c : chars) {
            String replacement = DIACRITICALS.get(String.valueOf(c));
            if (replacement != null) {
                result.append(replacement);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

}
