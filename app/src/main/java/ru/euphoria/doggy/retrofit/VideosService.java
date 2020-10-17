package ru.euphoria.doggy.retrofit;

import java.util.ArrayList;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.euphoria.doggy.api.model.Video;

public interface VideosService {
    @FormUrlEncoded
    @POST("video.get")
    Single<ArrayList<Video>> get(
            @Field("videos") String ids,
            @Field("count") int count,
            @Field("offset") int offset);
}
