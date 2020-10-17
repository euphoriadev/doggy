package ru.euphoria.doggy.common;

import java.util.HashMap;

/**
 * A class for holding links to objects that helps
 * when data doesn't fit completely on the {@link android.content.Intent}.
 * (Intent throws {@link android.os.TransactionTooLargeException} when data is large)
 * Like a big player list, or big text
 */
public class DataHolder {
    /** must be static to access from another class */
    private static HashMap<String, Object> objects = new HashMap<>();

    public static void setObject(String key, Object object) {
        objects.put(key, object);
    }

    public static Object getObject(String key) {
        return objects.get(key);
    }

    @Deprecated
    public static void setObject(Object object) {
        objects.put("single", object);
    }

    @Deprecated
    public static Object getObject() {
        return objects.get("single");
    }
}
