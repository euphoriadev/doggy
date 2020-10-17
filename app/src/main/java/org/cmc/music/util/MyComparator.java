/*
 * Modified By Romulus U. Ts'ai
 * Copied from MyComparator.java in SharedLib (http://www.fightingquaker.com/sharedlib/)
 * On Oct 6, 2008
 *
 */

package org.cmc.music.util;

import java.io.File;
import java.util.Comparator;

public abstract class MyComparator implements Comparator {
    public static final Comparator kToStringLengthReverse = (o1, o2) -> {
        if ((o1 == null) && (o2 == null))
            return 0;
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        String s1 = o1.toString();
        String s2 = o2.toString();

        return s2.length() - s1.length();
    };

    public static final Comparator kToStringHonorCase = (o1, o2) -> {
        if ((o1 == null) && (o2 == null))
            return 0;
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        String s1 = o1.toString();
        String s2 = o2.toString();

        return s1.compareTo(s2);
    };

    public static final Comparator kFileName = (o1, o2) -> {
        File f1 = (File) o1;
        File f2 = (File) o2;

        return f1.getName().toLowerCase().compareTo(
                f2.getName().toLowerCase());
    };

    public static final Comparator kFilePath = (o1, o2) -> {
        File f1 = (File) o1;
        File f2 = (File) o2;

        return f1.getAbsolutePath().toLowerCase().compareTo(
                f2.getAbsolutePath().toLowerCase());
    };

    public static final Comparator kFileNameReverse = (o1, o2) -> {
        File f1 = (File) o1;
        File f2 = (File) o2;

        return f2.getName().toLowerCase().compareTo(
                f1.getName().toLowerCase());
    };

    public static final Comparator kFilePathReverse = (o1, o2) -> {
        File f1 = (File) o1;
        File f2 = (File) o2;

        return f2.getAbsolutePath().toLowerCase().compareTo(
                f1.getAbsolutePath().toLowerCase());
    };
}