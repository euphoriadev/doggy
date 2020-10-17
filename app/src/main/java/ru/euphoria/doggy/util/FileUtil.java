package ru.euphoria.doggy.util;

import java.util.regex.Pattern;

public class FileUtil {
    public static final int FILE_TYPE_ARCHIVE = 0;
    public static final int FILE_TYPE_AUDIO = 1;
    public static final int FILE_TYPE_IMAGE = 2;
    public static final int FILE_TYPE_VIDEO = 3;
    public static final int FILE_TYPE_TEXT = 4;
    public static final int FILE_TYPE_EXECUTABLE = 5;
    public static final int FILE_TYPE_CODE = 6;
    public static final int FILE_TYPE_UNKNOWN = 100;

    public static final Pattern archive = Pattern.compile("(zip|rar|gz|7z|bz2|deb|pkg|jar)");
    public static final Pattern audio = Pattern.compile("(mp3|wav|ogg|flac|aac|wma)");
    public static final Pattern image = Pattern.compile("(jpeg|jpg|png|gif|bmp|svg|psd)");
    public static final Pattern video = Pattern.compile("(mp4|3gp|avi|flv|mkv|mov|mpeg)");
    public static final Pattern text = Pattern.compile("(txt|pdf|doc|docx|odt)");
    public static final Pattern docs = Pattern.compile("(doc|odt)");
    public static final Pattern databases = Pattern.compile("(db|sqlite|myd)");
    public static final Pattern executable = Pattern.compile("(apk|ipa|exe|bat)");
    public static final Pattern code = Pattern.compile("(java|php|htm|xml|bas|asp" +
            "|swift|lua|yaws|dart|go|js|pl|rb|rs|c|h|cpp|cs|py|sh|vb)");


    public static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    public static int getExtensionType(String ext) {
        if (find(archive, ext)) {
            return FILE_TYPE_ARCHIVE;
        }
        if (find(audio, ext)) {
            return FILE_TYPE_AUDIO;
        }
        if (find(image, ext)) {
            return FILE_TYPE_IMAGE;
        }
        if (find(video, ext)) {
            return FILE_TYPE_VIDEO;
        }
        if (find(text, ext)) {
            return FILE_TYPE_TEXT;
        }
        if (find(executable, ext)) {
            return FILE_TYPE_EXECUTABLE;
        }
        if (find(code, ext)) {
            return FILE_TYPE_CODE;
        }
        return FILE_TYPE_UNKNOWN;
    }

    private static boolean find(Pattern pattern, String input) {
        return pattern.matcher(input).find();
    }
}
