package ru.euphoria.doggy.retrofit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import ru.euphoria.doggy.api.VKApi;

public class RawJsonConverterAdapter implements Converter<ResponseBody, JSONObject> {
    public static Converter.Factory create() {
        return new Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (getRawType(type) == JSONObject.class) return new RawJsonConverterAdapter();
                return null;
            }
        };
    }

    public static Converter.Factory createForInteger() {
        return new Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (getRawType(type) == Integer.class) return new RawIntegerConverterAdapter();
                return null;
            }
        };
    }

    @Override
    public JSONObject convert(ResponseBody value) throws IOException {
        String body = value.string();
        try {
            JSONObject json = new JSONObject(body);
            VKApi.checkError(json, "");
            return json;
        } catch (JSONException e) {
            throw new IOException("Error when parsing json", e);
        }
    }

    private static class RawIntegerConverterAdapter implements Converter<ResponseBody, Integer> {
        private RawJsonConverterAdapter baseConverter;

        public RawIntegerConverterAdapter() {
            this.baseConverter = new RawJsonConverterAdapter();
        }

        @Override
        public Integer convert(ResponseBody value) throws IOException {
            return baseConverter.convert(value).optInt("response");
        }
    }
}
