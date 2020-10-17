package ru.euphoria.doggy.retrofit;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.euphoria.doggy.api.model.User;

public interface UsersService {

    @FormUrlEncoded @POST("users.get")
    Single<ArrayList<User>> get(
            @Field("user_ids") String ids,
            @Field("fields") String fields);


    @FormUrlEncoded @POST("users.get")
    Observable<ArrayList<User>> gets(
            @Field("user_ids") String ids,
            @Field("fields") String fields);
}
