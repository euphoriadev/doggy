package ru.euphoria.doggy.util;

import java.util.Locale;

public class TimeUtil {
    // like 12:20
    public static String formatSeconds(int secs) {
        return String.format(Locale.ROOT, "%02d:%02d", (secs % 3600) / 60, secs % 60);
    }
}
