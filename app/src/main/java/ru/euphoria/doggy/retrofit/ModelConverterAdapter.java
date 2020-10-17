package ru.euphoria.doggy.retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import ru.euphoria.doggy.util.VKUtil;

public class ModelConverterAdapter<T>
        implements Converter<ResponseBody, ArrayList<T>> {
    @SuppressWarnings("unchecked")
    public static <T> Converter.Factory create(T typedClass) {
        return new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                Type bound = getParameterUpperBound(0, (ParameterizedType) type);

                if (getRawType(type) == ArrayList.class && getRawType(bound) == typedClass)
                    return new ModelConverterAdapter((Class<T>) typedClass);
                return null;
            }
        };
    }

    private Class<T> typedClass;
    private RawJsonConverterAdapter jsonConverter;

    public ModelConverterAdapter(Class<T> typedClass) {
        this.jsonConverter = new RawJsonConverterAdapter();
        this.typedClass = typedClass;
    }

    @Override
    public ArrayList<T> convert(ResponseBody value) throws IOException {
        return VKUtil.from(typedClass, jsonConverter.convert(value));
    }
}
