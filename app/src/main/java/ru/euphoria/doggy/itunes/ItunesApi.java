package ru.euphoria.doggy.itunes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ItunesApi {
    @GET("https://itunes.apple.com/search?limit=1")
    Call<SearchResult> searchAlbum(@Query("term") String term);
}
