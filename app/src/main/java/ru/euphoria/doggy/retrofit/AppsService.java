package ru.euphoria.doggy.retrofit;

import java.util.ArrayList;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.euphoria.doggy.api.model.App;

public interface AppsService {
    @FormUrlEncoded
    @POST("apps.get")
    Single<ArrayList<App>> get(@Field("app_id") int id);
}
