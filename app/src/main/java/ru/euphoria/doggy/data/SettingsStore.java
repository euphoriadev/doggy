package ru.euphoria.doggy.data;

import android.os.Environment;

import java.util.Set;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.SettingsFragment;

/**
 * Created by admin on 23.03.18.
 */

public class SettingsStore {
    public static final String KEY_USE_AUDIO_CACHE = "audio_cache";
    public static final String KEY_ONLINE_ACCESS_TOKEN = "online_access_token";
    public static final String KEY_AUDIO_ACCESS_TOKEN = "audio_access_token";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_CHECK_SAVE_PASSWORD = "is_save_password";
    public static final String KEY_MSG_IGNORE_LIST = "msg_ignore_list";

    public static boolean has(String key) {
        return AppContext.preference.contains(key);
    }

    public static long getLong(String key) {
        return AppContext.preference.getLong(key, -1);
    }

    public static int getInt(String key) {
        return getInt(key, -1);
    }

    public static int getInt(String key, int defValues) {
        return AppContext.preference.getInt(key, defValues);
    }

    public static float getFloat(String key) {
        return AppContext.preference.getFloat(key, -1);
    }

    public static String getString(String key) {
        return AppContext.preference.getString(key, "");
    }

    public static String getString(String key, String defValue) {
        return AppContext.preference.getString(key, defValue);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return AppContext.preference.getBoolean(key, defaultValue);
    }

    public static Set<String> getStringSet(String key, Set<String> defaultValue) {
        return AppContext.preference.getStringSet(key, defaultValue);
    }

    public static Set<String> getStringSet(String key) {
        return getStringSet(key, null);
    }

    public static void putValue(String key, boolean value) {
        AppContext.preference.edit().putBoolean(key, value).apply();
    }

    public static void putValue(String key, int value) {
        AppContext.preference.edit().putInt(key, value).apply();
    }

    public static void putValue(String key, long value) {
        AppContext.preference.edit().putLong(key, value).apply();
    }

    public static void putValue(String key, float value) {
        AppContext.preference.edit().putFloat(key, value).apply();
    }

    public static void putValue(String key, String value) {
        AppContext.preference.edit().putString(key, value).apply();
    }

    public static void putValue(String key, Set<String> value) {
        AppContext.preference.edit().putStringSet(key, value).apply();
    }

    public static void clear() {
        AppContext.preference.edit().clear().apply();
    }

    public static void remove(String key) {
        AppContext.preference.edit().remove(key).apply();
    }

    public static boolean nightMode() {
        return getBoolean(SettingsFragment.KEY_NIGHT_MODE);
    }

    public static int getUserId() {
        return getInt(KEY_USER_ID);
    }

    public static String getAccessToken() {
        return getString(KEY_ACCESS_TOKEN);
    }

    public static String getOnlineAccessToken() {
        return getString(KEY_ONLINE_ACCESS_TOKEN, getAccessToken());
    }

    public static String getAudioAccessToken() {
        return getString(KEY_AUDIO_ACCESS_TOKEN);
    }

    public static String getPurchaseToken() {
        return getString("purchase_token");
    }

    public static String getLogin() {
        return getString(KEY_LOGIN);
    }

    public static String getPassword() {
        return getString(KEY_PASSWORD);
    }

    public static boolean useCache() {
        return getBoolean(KEY_USE_AUDIO_CACHE, false);
    }

    public static boolean useOptimizedSqlite() {
        return getBoolean(SettingsFragment.KEY_OPTIMIZED_SQLITE, false);
    }

    public static String getMusicFolder() {
        return SettingsStore.getString(SettingsFragment.KEY_MUSIC_FOLDER, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
    }
}
