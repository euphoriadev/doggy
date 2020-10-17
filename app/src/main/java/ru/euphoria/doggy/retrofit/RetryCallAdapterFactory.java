package ru.euphoria.doggy.retrofit;


import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.euphoria.doggy.api.VKException;

public class RetryCallAdapterFactory extends CallAdapter.Factory {
    private static final String TAG = "RetryCallback";
    private static final int MAX_RETRIES = 3;
    private static final int SLEEP_AFTER_ERROR = 400;

    public static RetryCallAdapterFactory create() {
        return new RetryCallAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Log.d(TAG, "Starting a CallAdapter with retries");
        return new RetryCallAdapter<>(
                retrofit.nextCallAdapter(this, returnType, annotations)
        );
    }

    private static class RetryCallAdapter<R, T> implements CallAdapter<R, T> {
        private final CallAdapter<R, T> delegate;

        private RetryCallAdapter(CallAdapter<R, T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Type responseType() {
            return delegate.responseType();
        }

        @Override
        public T adapt(Call<R> call) {
            return delegate.adapt(new RetryingCall<R>(call));
        }
    }

    private static class RetryingCall<R> implements Call<R> {
        private final Call<R> delegate;

        private RetryingCall(Call<R> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Response<R> execute() throws IOException {
            for (int i = 0;; i++) {
                try {
                    return delegate.isExecuted() ? delegate.clone().execute() : delegate.execute();
                } catch (Exception e) {
                    if (i >= MAX_RETRIES) {
                        throw e;
                    }
                    if (e instanceof VKException) {
                        Log.w(TAG, String.format("Call execute failed. %d/%d Retrying...", i + 1, MAX_RETRIES), e);
                        try {
                            Thread.sleep(SLEEP_AFTER_ERROR);
                        } catch (InterruptedException ignored) {
                        }
                    } else {
                        throw e;
                    }
                }
            }
        }

        @Override
        public void enqueue(Callback<R> callback) {
            delegate.enqueue(new RetryCallback<>(callback, delegate));
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @Override
        public Call<R> clone() {
            return new RetryingCall<>(delegate);
        }

        @Override
        public Request request() {
            return delegate.request();
        }
    }

    private static class RetryCallback<T> implements Callback<T> {
        private static final String TAG = "RetryCallback";

        private AtomicInteger counter = new AtomicInteger(0);
        private Callback<T> callback;
        private Call<T> call;

        public RetryCallback(Callback<T> callback, Call<T> call) {
            this.callback = callback;
            this.call = call;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (!response.isSuccessful() && counter.incrementAndGet() <= MAX_RETRIES) {
                Log.w(TAG, "Call with no success result code: " +response.code());
                retryCall();
            } else {
                callback.onResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            Log.w(TAG, "Call failed with message: " + t.getLocalizedMessage(), t);
            if (counter.incrementAndGet() <= MAX_RETRIES) {
                retryCall();
            } else {
                callback.onFailure(call, t);
            }
        }

        private void retryCall() {
            try {
                Thread.sleep(SLEEP_AFTER_ERROR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, String.format("%d/%d Retrying...", counter.get(), MAX_RETRIES));
            call.clone().enqueue(this);
        }
    }
}