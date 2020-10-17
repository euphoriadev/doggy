package ru.euphoria.doggy.yandex;

import com.yandex.metrica.YandexMetrica;

import org.json.JSONObject;

import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.common.Tokens;

public class Translator {
    private static final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static final String API_KEY = Tokens.YANDEX_TRANSLATOR_API_KEY;

    public static Single<String> translate(String text) {
        return Single.fromCallable(() -> {
            FormBody.Builder body = new FormBody.Builder();
            body.add("key", API_KEY);
            body.add("text", text);
            body.add("lang", Locale.getDefault().getLanguage());
            body.add("format", "plain");

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .method("POST", body.build())
                    .build();

            Response response = AppContext.httpClient.newCall(request).execute();
            String string = response.body().string();
            JSONObject json = new JSONObject(string);
            if (json.optInt("code") == 200) {
                YandexMetrica.reportEvent("Перевод слов трека");
            }
            return json
                    .optJSONArray("text")
                    .optString(0);
        }).subscribeOn(Schedulers.io());
    }
}
