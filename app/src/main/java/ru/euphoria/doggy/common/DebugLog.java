package ru.euphoria.doggy.common;

import android.util.Log;

import ru.euphoria.doggy.BuildConfig;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static android.util.Log.getStackTraceString;

/**
 * Print logs when only build config is debug.
 * <p>
 * {@link BuildConfig#DEBUG}
 */
public class DebugLog {
    private static final boolean enabled = BuildConfig.DEBUG;

    public static int v(String tag, String msg) {
        return println(VERBOSE, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return println(VERBOSE, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int d(String tag, String msg) {
        return println(DEBUG, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return println(DEBUG, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int i(String tag, String msg) {
        return println(INFO, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return println(INFO, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int w(String tag, String msg) {
        return println(WARN, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return println(WARN, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int w(String tag, Throwable tr) {
        return println(WARN, tag, getStackTraceString(tr));
    }

    public static int e(String tag, String msg) {
        return println(ERROR, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return println(ERROR, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int println(int priority, String tag, String msg) {
        return enabled ? Log.println(priority, tag, msg) : 0;
    }
}
