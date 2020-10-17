package ru.euphoria.doggy.retrofit;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ExecutesService {
    @FormUrlEncoded
    @POST("execute")
    Call<JSONObject> execute(@Field("code") String code);

    @FormUrlEncoded
    @POST("{method}")
    Call<JSONObject> method(@Path("method") String method, @FieldMap Map<String, String> params);
}
