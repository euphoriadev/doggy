package ru.euphoria.doggy.itunes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class AudiosAlbumConverterAdapter implements Converter<ResponseBody, SearchResult> {
    public static Converter.Factory create() {
        return new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (getRawType(type) == SearchResult.class) return new AudiosAlbumConverterAdapter();
                return null;
            }
        };
    }

    @Override
    public SearchResult convert(ResponseBody value) throws IOException {
        JSONObject json;
        try {
            json = new JSONObject(value.string());
            JSONArray results = json.optJSONArray("results");
            if (results == null || results.length() == 0) {
                throw new IOException("No result for this song");
            }

            JSONObject track = results.getJSONObject(0);
            return new SearchResult(track);
        } catch (Exception e) {
            throw new IOException("No images for this song");
        }
    }
}