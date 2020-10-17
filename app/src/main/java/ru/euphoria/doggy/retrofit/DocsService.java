package ru.euphoria.doggy.retrofit;

import org.json.JSONObject;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface DocsService {

    /**
     * Получает адрес сервера для загрузки документа в личное сообщение.
     *
     * @param type    тип документа. Возможные значения:
     *                doc — обычный документ;
     *                audio_message — голосовое сообщение;
     *                graffiti — граффити.
     * @param peer_id идентификатор назначения
     */
    @FormUrlEncoded @POST("docs.getMessagesUploadServer")
    Single<JSONObject> getMessagesUploadServer(@Field("type") String type,
                                               @Field("peer_id") int peer_id);

    /**
     * Сохраняет документ после его успешной загрузки на сервер.
     *
     * @param file  параметр, возвращаемый в результате загрузки файла на сервер
     * @param title название документа
     * @param tags  метки для поиска
     */
    @FormUrlEncoded @POST("docs.save")
    Single<JSONObject> save(@Field("file") String file,
                            @Field("title") String title,
                            @Field("tags") String tags);
}
