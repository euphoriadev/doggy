package ru.euphoria.doggy.common;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.Single;
import okhttp3.Request;
import ru.euphoria.doggy.api.Authorizer;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.VKException;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;

import static ru.euphoria.doggy.data.SettingsStore.KEY_LOGIN;

@SuppressLint("CheckResult")
public class DirectLogin {
    public static final ArrayList<Platform> platforms = new ArrayList<>(5);

    static {
        platforms.add(new Platform("Android", 2274003, "hHbZxrka2uZ6jB1inYsH"));
        platforms.add(new Platform("iPhone", 3140623, "VeWdmVclDCtn6ihuP1nt"));
        platforms.add(new Platform("iPad", 3682744, "mY6CDUswIVdJLCD3j15n"));
        platforms.add(new Platform("Windows", 3697615, "AlVXZFMUqyrnABp8ncuU"));
        platforms.add(new Platform("Windows Phone", 3502557, "PEObAuQi6KloPM4T30DV"));
        platforms.add(new Platform("Kate Mobile", 2685278, "lxhD8OD7dMsqtXIm5IUY"));
    }

    public static Platform platform(int id) {
        for (Platform p : platforms) {
            if (p.id == id) {
                return p;
            }
        }
        throw new NullPointerException("platform not found");
    }

    public static void auth(String login, String password,
                            Platform platform, String api, String userAgent, String code,
                            OnResponseListener responseListener, OnErrorListener errorListener) {
        String url = Authorizer.getDirectUrl(platform.id, platform.secret, login, password, api, code, null, null);
        Request.Builder builder = new Request.Builder()
                .url(url);
        if (!TextUtils.isEmpty(userAgent)) {
            builder.header("User-Agent", userAgent);
        }

        AndroidUtil.request(builder.build())
                .map(JSONObject::new)
                .flatMap(json -> {
                    VKApi.checkError(json, url);
                    return Single.just(json);
                })
                .subscribe(json -> {
                    String token = json.optString("access_token");
                    responseListener.onResponse(token);

                    SettingsStore.putValue(KEY_LOGIN, login);
                    if (SettingsStore.getBoolean("save_password")) {
                        SettingsStore.putValue(SettingsStore.KEY_PASSWORD, password);
                    }
                }, error -> {
                    if (error instanceof VKException) {
                        VKException ex = (VKException) error;
                        if (ex.getMessage().contains("sms sent")
                                || ex.getMessage().contains("app code")) {
                            errorListener.onError(true, null);
                        }
                    }
                });
    }

    public static class Platform {
        public String platform;
        public int id;
        public String secret;

        public Platform(String platform, int id, String secret) {
            this.platform = platform;
            this.id = id;
            this.secret = secret;
        }

        @Override
        public String toString() {
            return platform;
        }
    }

    public interface OnResponseListener {
        void onResponse(String accessToken) throws Exception;
    }

    public interface OnErrorListener {
        void onError(boolean needCode, String captcha) throws Exception;
    }
}
