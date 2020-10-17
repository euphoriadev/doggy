package ru.euphoria.doggy.retrofit;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import okhttp3.Interceptor;
import okhttp3.Response;
import okio.BufferedSink;

public class ParamsInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        return null;
    }

    public void addParam(BufferedSink buffer, String key, String value) throws IOException {
        buffer.writeString(MessageFormat.format("&{0}={1}", key, value), Charset.defaultCharset());
    }
}
