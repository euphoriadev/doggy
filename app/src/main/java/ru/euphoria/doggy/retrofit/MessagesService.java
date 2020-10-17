package ru.euphoria.doggy.retrofit;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MessagesService {

    /**
     * Отправляет сообщение
     *
     * @param peer_id    идентификатор назначения
     * @param random_id  уникальный идентификатор предназначенный для предотвращения повторной отправки одинакового сообщения
     * @param message    текст личного сообщения
     * @param attachment медиавложения к личному сообщению, перечисленные через запятую
     */
    @FormUrlEncoded
    @POST("messages.send")
    Single<Integer> send(@Field("peer_id") int peer_id,
                         @Field("random_id") int random_id,
                         @Field("message") String message,
                         @Field("attachment") String attachment);
}
