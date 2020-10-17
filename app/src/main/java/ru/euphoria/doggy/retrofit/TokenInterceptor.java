package ru.euphoria.doggy.retrofit;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.data.SettingsStore;

public class TokenInterceptor extends ParamsInterceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Sink sink = Okio.sink(new ByteArrayOutputStream());
        BufferedSink buffer = Okio.buffer(sink);

        RequestBody body = chain.request().body();
        if (body != null) {
            body.writeTo(buffer);
            addParam(buffer, "access_token", SettingsStore.getAccessToken());
            addParam(buffer, "v", String.valueOf(VKApi.API_VERSION));

            RequestBody requestBody = RequestBody.create(
                    buffer.getBuffer().readUtf8(),
                    body.contentType()
            );
            return chain.proceed(chain.request().newBuilder().post(requestBody).build());
        }
        return chain.proceed(chain.request());
    }
}
