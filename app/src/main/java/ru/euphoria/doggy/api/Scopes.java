package ru.euphoria.doggy.api;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Scopes constants used for authorization, see http://vk.com/dev/permissions.
 */
public class Scopes {
    /**
     * User allowed to send notifications to him/her
     */
    public static final String NOTIFY = "notify";

    /**
     * Access to friends
     */
    public static final String FRIENDS = "friends";

    /**
     * Access to photos
     */
    public static final String PHOTOS = "photos";

    /**
     * Access to audios
     */
    public static final String AUDIO = "audio";

    /**
     * Access to videos
     */
    public static final String VIDEO = "video";

    /**
     * Access to documents
     */
    public static final String DOCS = "docs";

    /**
     * Access to user notes
     */
    public static final String NOTES = "notes";

    /**
     * Access to wiki pages
     */
    public static final String PAGES = "pages";

    /**
     * Access to user status
     */
    public static final String STATUS = "status";

    /**
     * Access to standard and advanced methods for the wall
     */
    public static final String WALL = "wall";

    /**
     * Access to user groups
     */
    public static final String GROUPS = "groups";

    /**
     * Access to advanced methods for messaging
     */
    public static final String MESSAGES = "messages";

    /**
     * Access to notifications about answers to the user
     */
    public static final String NOTIFICATIONS = "notifications";

    /**
     * Access to statistics of user groups and applications where he/she is an administrator.
     */
    public static final String STATS = "stats";

    /**
     * Access to advanced methods for Ads API
     */
    public static final String ADS = "ads";

    /**
     * Access to API at any time
     */
    public static final String OFFLINE = "offline";

    /**
     * User e-mail access. Available only for sites
     */
    public static final String EMAIL = "email";

    /**
     * Possibility to make API requests without HTTPS.
     */
    @Deprecated
    public static final String NO_HTTPS = "nohttps";

    /**
     * Access to direct authorization
     */
    public static final String DIRECT = "direct";

    /**
     * Converts integer value of permissions into {@link ArrayList} of constants
     *
     * @param permissions integer permissions value
     */
    public static ArrayList<String> parse(int permissions) {
        ArrayList<String> res = new ArrayList<>(16);
        if ((permissions & 1) > 0) res.add(NOTIFY);
        if ((permissions & 2) > 0) res.add(FRIENDS);
        if ((permissions & 4) > 0) res.add(PHOTOS);
        if ((permissions & 8) > 0) res.add(AUDIO);
        if ((permissions & 16) > 0) res.add(VIDEO);
        if ((permissions & 128) > 0) res.add(PAGES);
        if ((permissions & 1024) > 0) res.add(STATUS);
        if ((permissions & 2048) > 0) res.add(NOTES);
        if ((permissions & 4096) > 0) res.add(MESSAGES);
        if ((permissions & 8192) > 0) res.add(WALL);
        if ((permissions & 32768) > 0) res.add(ADS);
        if ((permissions & 65536) > 0) res.add(OFFLINE);
        if ((permissions & 131072) > 0) res.add(DOCS);
        if ((permissions & 262144) > 0) res.add(GROUPS);
        if ((permissions & 524288) > 0) res.add(NOTIFICATIONS);
        if ((permissions & 1048576) > 0) res.add(STATS);
        if ((permissions & 4194304) > 0) res.add(EMAIL);
        return res;
    }

    public static String from(String... scopes) {
        return TextUtils.join(",", scopes);
    }

    /**
     * Returns all permissions as String separate by ','.
     */
    public static String all() {
        return NOTIFY + ',' + FRIENDS
                + ',' + PHOTOS + ',' + AUDIO
                + ',' + VIDEO + ',' + PAGES
                + ',' + STATUS + ',' + NOTES
                + ',' + MESSAGES + ',' + WALL
                + ',' + ADS + ',' + OFFLINE
                + ',' + DOCS + ',' + GROUPS
                + ',' + NOTIFICATIONS + ',' + STATS
                + ',' + EMAIL;
    }
}