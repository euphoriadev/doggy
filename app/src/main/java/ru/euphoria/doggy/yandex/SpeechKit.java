package ru.euphoria.doggy.yandex;

import android.util.Log;

import com.yandex.metrica.YandexMetrica;

import org.json.JSONObject;

import java.io.File;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.BuildConfig;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.Tokens;

public class SpeechKit {
    private static final String TAG = "Yandex.SpeechKit";

    private static final String BASE_URL = "https://stt.api.cloud.yandex.net/speech/v1/stt:recognize";
    private static final String OAUTH_TOKEN = Tokens.YANDEX_SPEECHKIT_OAUTH_TOKEN;
    private static final String OAUTH_TO_IAM = "{\"yandexPassportOauthToken\": \"%s\"}";
    private static final String MEDIA_TYPE = "application/json";
    private static final String FOLDER_ID = "b1g1592aneu6fms6q13c";

    private static String iamToken;

    private static String iamToken() throws Exception {
        if (iamToken != null) {
            return iamToken;
        }
        String json = String.format(OAUTH_TO_IAM, OAUTH_TOKEN);
        RequestBody body = RequestBody.create(MediaType.get(MEDIA_TYPE), json);

        Request request = new Request.Builder()
                .url("https://iam.api.cloud.yandex.net/iam/v1/tokens")
                .post(body)
                .build();

        String response = AppContext.httpClient.newCall(request).execute().body().string();
        iamToken = new JSONObject(response).optString("iamToken");

        DebugLog.w(TAG, "iam token: " + iamToken);
        return iamToken;
    }


    public static String getText(File voice) throws Exception {
        HttpUrl url = HttpUrl.get(BASE_URL).newBuilder()
                .addQueryParameter("topic", "general")
                .addQueryParameter("profanityFilter", "false")
                .addQueryParameter("folderId", FOLDER_ID)
                .build();

        RequestBody body = RequestBody.create(MediaType.get("audio/ogg"), voice);
        Request request = new Request.Builder()
                .url(url).addHeader("Authorization", "Bearer " + iamToken())
                .post(body).build();

        String response = AppContext.httpClient.newCall(request).execute().body().string();
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "voice response: " + response);
            YandexMetrica.reportEvent("Перевод голоса в текст");
        }
        return new JSONObject(response).optString("result");
    }
}
