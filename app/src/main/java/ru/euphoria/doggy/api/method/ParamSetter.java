package ru.euphoria.doggy.api.method;

import android.text.TextUtils;
import android.util.ArrayMap;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;

/**
 * Created by admin on 24.03.18.
 */

public class ParamSetter<E> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected ArrayMap<String, String> params = new ArrayMap<>();
    protected String userAgent;
    protected String method;
    protected HttpUrl.Builder url;

    public ParamSetter(String method) {
        this.method = method;
        this.url = HttpUrl.get(String.format("https://%s/method/%s", VKApi.apiDomain, method))
                .newBuilder();

        this.put("v", VKApi.API_VERSION);
        this.put("access_token", SettingsStore.getAccessToken());
        this.put("lang", Locale.getDefault().getLanguage());
        this.userAgent(AndroidUtil.getKateUserAgent());
    }

    public ParamSetter<E> put(String key, String value) {
        params.put(key, value);
        return this;
    }

    public ParamSetter<E> put(String key, int value) {
        params.put(key, String.valueOf(value));
        return this;
    }

    public ParamSetter<E> put(String key, double value) {
        params.put(key, String.valueOf(value));
        return this;
    }

    public ParamSetter<E> peerId(int value) {
        return put("peer_id", value);
    }

    public ParamSetter<E> peerId(long value) {
        return put("peer_id", value);
    }

    public ParamSetter<E> userId(int value) {
        return put("user_id", value);
    }

    public ParamSetter<E> ownerId(int value) {
        return put("owner_id", value);
    }

    public ParamSetter<E> audioId(int value) {
        return put("audio_id", value);
    }

    public ParamSetter<E> artist(String value) {
        return put("artist", value);
    }

    public ParamSetter<E> title(String value) {
        return put("title", value);
    }

    public ParamSetter<E> text(String value) {
        return put("text", value);
    }

    public ParamSetter<E> genreId(int value) {
        return put("genre_id", value);
    }

    public ParamSetter<E> groupId(int value) {
        return put("group_id", value);
    }

    public ParamSetter<E> groupIds(int... value) {
        return put("group_ids", ArrayUtil.join(value, ','));
    }

    public ParamSetter<E> chatId(int value) {
        return put("chat_id", value);
    }

    public ParamSetter<E> chatIds(int... value) {
        return put("chat_ids", ArrayUtil.join(value, ','));
    }

    public ParamSetter<E> userIds(int... values) {
        return put("user_ids", ArrayUtil.join(values, ','));
    }

    public ParamSetter<E> messageIds(int... values) {
        return put("message_ids", ArrayUtil.join(values, ','));
    }

    public ParamSetter<E> lat(double value) {
        return put("lat", value);
    }

    public ParamSetter<E> lng(double value) {
        return put("long", value);
    }

    public ParamSetter<E> radius(int value) {
        return put("radius", value);
    }

    public ParamSetter<E> fields(String value) {
        return put("fields", value);
    }

    public ParamSetter<E> count(int value) {
        return put("count", value);
    }

    public ParamSetter<E> offset(int value) {
        return put("offset", value);
    }

    public ParamSetter<E> message(String value) {
        return put("message", value);
    }

    public ParamSetter<E> extended(boolean value) {
        return put("extended", value ? 1 : 0);
    }

    public ParamSetter<E> type(String value) {
        return put("type", value);
    }

    public ParamSetter<E> accessToken(String value) {
        return put("access_token", value);
    }

    public ParamSetter<E> v(String value) {
        return put("v", value);
    }

    public ParamSetter<E> userAgent(String value) {
        this.userAgent = value;
        return this;
    }

    public Request request() {
        Request.Builder builder = new Request.Builder()
                .method("POST", getFormBody())
                .url(url.build());
        if (!TextUtils.isEmpty(userAgent)) {
            builder.header("User-Agent", userAgent);
        }
        Request build = builder.build();
        return build;
    }

    private FormBody getFormBody() {
        FormBody.Builder body = new FormBody.Builder();
        for (int i = 0; i < params.size(); i++) {
            String name = params.keyAt(i);
            String value = params.valueAt(i);

            body.add(name, value);
        }
        return body.build();
    }

    public String body() throws IOException {
        return VKApi.body(request());
    }

    public JSONObject json() throws Exception {
        return VKApi.json(request());
    }

    public Flowable<ArrayList<E>> async(Class<E> parseClass) {
        return Flowable.fromCallable(()
                -> VKApi.from(parseClass, json()))
                .subscribeOn(Schedulers.io());
    }

    public Flowable<E> asyncSingle(Class<E> parseClass) {
        return Flowable.fromCallable(() -> {
            JSONObject json = json();
            JSONObject object = VKApi.optJsonObject(json);
            return VKApi.fromSingle(parseClass, object);
        }).subscribeOn(Schedulers.io());
    }

    public String method() {
        return method;
    }

    public ArrayMap<String, String> params() {
        return params;
    }
}
