package ru.euphoria.doggy.api;

import android.text.TextUtils;

import okhttp3.HttpUrl;

/**
 * Created by admin on 24.03.18.
 */

public class Authorizer {
    public static String getDirectUrl(int clientId, String secret, String username, String password, String api, String code, String captcha_sid, String captcha_key) {
        HttpUrl.Builder builder = HttpUrl.parse("https://" + VKApi.oauthDomain + "/token").newBuilder()
                .addQueryParameter("grant_type", "password")
                .addQueryParameter("client_id", String.valueOf(clientId))
                .addQueryParameter("client_secret", secret)
                .addQueryParameter("username", username)
                .addQueryParameter("password", password)
                .addQueryParameter("scope", Scopes.all())
                .addQueryParameter("v", api)
                .addQueryParameter("2fa_supported", "1");

        if (!TextUtils.isEmpty(code)) {
            builder.addQueryParameter("code", code);
        }
        if (!TextUtils.isEmpty(captcha_key)) {
            builder.addQueryParameter("captcha_key", captcha_key);
        }
        if (!TextUtils.isEmpty(captcha_sid)) {
            builder.addQueryParameter("captcha_sid", captcha_sid);
        }
        return builder.toString();
    }
}
