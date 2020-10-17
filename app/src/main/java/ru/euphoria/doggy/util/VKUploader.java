package ru.euphoria.doggy.util;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ru.euphoria.doggy.AppContext;

@SuppressLint("CheckResult")
public class VKUploader {
    private static final String TAG = "VKUploader";

    public static final String TYPE_DOC = "doc";
    public static final String TYPE_AUDIO_MESSAGE = "audio_message";
    public static final String TYPE_GRAFFITI = "graffiti";

    public static Single<JSONObject> audioMessage(File file, int peer_id) {
        return document(file, TYPE_AUDIO_MESSAGE, "audio_message", "", peer_id);
    }

    public static Single<JSONObject> document(File file, String type, String title, String tags, int peer_id) {
        return AppContext.docs.getMessagesUploadServer(type, peer_id)
                .map(json -> json.optJSONObject("response"))
                .map(json -> json.optString("upload_url"))
                .flatMap(url -> Single.just(post(file, title, url)))
                .map(ResponseBody::string)
                .map(JSONObject::new)
                .map(json -> json.optString("file"))
                .flatMap(jsonFile -> AppContext.docs.save(jsonFile, title, tags));
    }

    private static ResponseBody post(File file, String title, String uploadUrl) throws IOException {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", title,
                        RequestBody.create(file, MediaType.parse("multipart/form-data")))
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(body)
                .build();

        return AppContext.httpClient.newCall(request).execute().body();
    }
}
