package ru.euphoria.doggy.util;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.ErrorCodes;
import ru.euphoria.doggy.api.VKException;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;

public class VKUtil {
    public static final String TAG = "VKUtil";

    /**
     * Получает все элементы.
     * <p>
     * Работает в методах, где в ответе есть count и items или users.
     * За один запрос получает maxCount * 25 элементов
     *
     * @param method   имя метода, например users.get
     * @param maxCount максимальное количество элементов, которое можно получить за один запрос
     * @param offset   отступ от начала списка
     * @param values   параметры запроса, например peer_id, user_ids
     * @param key      ключ элементов, которые нужно получить, обычно items
     * @param <T>      тип обьекта, например {@link ru.euphoria.doggy.api.model.User}
     */
    public static <T> Flowable<ArrayList<T>> getAll(String method, int maxCount, int offset, HashMap<String, String> values, String key, Class<T> typedClass) {
        // не получается модицифировать offset нормально
        int[] offsets = new int[]{offset};
        return Flowable.create(emitter -> {
            int count = 0;
            try {
                while (true) {
                    String script = getExecuteScript(method, maxCount, offsets[0], values, key,
                            count > 0 ? String.valueOf(count) : "null");

                    JSONObject json = AppContext.executes.execute(script).execute().body();
                    JSONObject response = json.optJSONObject("response");
                    ArrayList<T> list = from(typedClass, response);
                    emitter.onNext(list);

                    boolean more = response.optBoolean("more");
                    if (!more) {
                        break;
                    }

                    offsets[0] = response.optInt("offset");
                    count = response.optInt("count");
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * Получает все элементы без использования execute
     * Полезно если response слишком большой и выкидывает ошибку.
     *
     * @param method   имя метода, например users.get
     * @param maxCount максимальное количество элементов, которое можно получить за один запрос
     * @param offset   отступ от начала списка
     * @param values   параметры запроса, например peer_id, user_ids
     * @param <T>      тип обьекта, например {@link ru.euphoria.doggy.api.model.User}
     */
    public static <T> Flowable<ArrayList<T>> getAllSlow(String method, int maxCount, int offset, HashMap<String, String> values, Class<T> typedClass) {
        // не получается модицифировать offset нормально
        int[] offsets = new int[]{offset};

        return Flowable.create(emitter -> {
            try {
                while (true) {
                    values.put("offset", String.valueOf(offsets[0]));
                    values.put("count", String.valueOf(maxCount));

                    JSONObject json = AppContext.executes.method(method, values).execute().body();

                    JSONObject response = json.optJSONObject("response");
                    JSONArray items = optJsonArray(response);

                    if (items.length() == 0) {
                        break;
                    }
                    ArrayList<T> list = from(typedClass, items);
                    emitter.onNext(list);

                    offsets[0] += items.length();
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static Flowable<ArrayList<Message>> getAllHistory(int peer_id, int offset) {
        HashMap<String, String> params = new HashMap<>();
        params.put("peer_id", String.valueOf(peer_id));
        params.put("rev", "1");

        return getAll("messages.getHistory", 200, offset, params, "items", Message.class);
    }

    public static Flowable<ArrayList<Message>> getAllSlowHistory(int peer_id, int offset) {
        HashMap<String, String> params = new HashMap<>();
        params.put("peer_id", String.valueOf(peer_id));
        params.put("rev", "1");

        return getAllSlow("messages.getHistory", 200, offset, params, Message.class);
    }

    public static Flowable<ArrayList<User>> getAllFriends(int user_id, int offset, String order) {
        HashMap<String, String> params = new HashMap<>();
        params.put(quote("user_id"), quote(user_id));
        params.put(quote("fields"), quote(User.DEFAULT_FIELDS));
        params.put(quote("order"), quote(order));

        return getAll("friends.get", 5000, offset, params, "items", User.class);
    }


    public static <E> ArrayList<E> from(Class<E> typedClass, JSONObject json) {
        return from(typedClass, optJsonArray(json));
    }

    public static <E> ArrayList<E> from(Class<E> typedClass, JSONArray array) {
        return from(typedClass, array, null);
    }

    public static <E> ArrayList<E> from(Class<E> typedClass, JSONArray array, String name) {
        ArrayList<E> list = new ArrayList<>(array == null ? 0 : array.length());
        if (array == null) {
            return list;
        }

        try {
            Constructor<E> constructor = typedClass.getConstructor(JSONObject.class);
            constructor.setAccessible(true);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (!TextUtils.isEmpty(name) && item.has(name)) {
                    item = item.optJSONObject(name);
                }
                list.add(constructor.newInstance(item));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void checkError(JSONObject json, String url) throws VKException {
        if (!json.has("error")) {
            return;
        }

        JSONObject error = json.optJSONObject("error");
        if (error == null) {
            checkDirectAuthError(json, url);
            return;
        }

        int code = error.optInt("error_code");
        String message = error.optString("error_msg");

        VKException e = new VKException(url, message, code);
        if (code == ErrorCodes.CAPTCHA_NEEDED) {
            e.captchaImg = error.optString("captcha_img");
            e.captchaSid = error.optString("captcha_sid");
        }
        if (code == ErrorCodes.VALIDATION_REQUIRED) {
            e.redirectUri = error.optString("redirect_uri");
        }
        throw e;
    }

    public static void checkDirectAuthError(JSONObject json, String url) throws VKException {
        String error = json.optString("error");
        String message = json.optString("error_description");

        VKException ex = new VKException(url, error, message);
        if ("need_captcha".equals(error)) {
            ex.message = "Need Captcha";
            ex.code = ErrorCodes.CAPTCHA_NEEDED;
            ex.captchaImg = json.optString("captcha_img");
            ex.captchaSid = json.optString("captcha_sid");
        }
        throw ex;
    }

    public static int[] parseArray(JSONArray array) {
        if (array == null || array.length() == 0) return null;

        int[] list = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            list[i] = array.optInt(i);
        }
        return list;
    }

    public static int optCount(JSONObject root) {
        return root.optJSONObject("response").optInt("count");
    }

    public static JSONArray optJsonArray(JSONObject json) {
        return optJsonArray(json, "items");
    }

    public static JSONArray optJsonArray(JSONObject root, String name) {
        Object response = root.opt("response");
        if (response instanceof JSONArray) {
            return (JSONArray) response;
        }
        if (root.has(name)) {
            return root.optJSONArray(name);
        }

        return root.optJSONObject("response").optJSONArray(name);
    }

    public static String getExecuteScript(String method, int maxCount, int offset,
                                          HashMap<String, String> values, String key, String totalCount) {
        String script = AndroidUtil.loadAssestsFile("execute_code.txt");
        values.put("count", String.valueOf(maxCount));
        return script.replace("%(values)s", values.toString()
                .replace('=', ':'))
                .replace("%(method)s", method)
                .replace("%(count)s", totalCount)
                .replace("%(offset)s", String.valueOf(offset))
                .replace("%(offset_mul)s", "1")
                .replace("%(key)s", key);
    }

    public static String quote(String value) {
        return "\"" + value + "\"";
    }

    public static String quote(int value) {
        return "\"" + value + "\"";
    }

    public static String script(Context context, String script, Object... params) {
        try {
            return String.format(AndroidUtil.loadAssestsFile(context, script),
                    params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
