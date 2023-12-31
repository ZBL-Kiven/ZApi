package com.zj.ok3;

import android.os.Build;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


// Only added when CompletableFuture is available (Java 8+ / Android API 24+).
final class CompletableFutureCallAdapterFactory extends CallAdapter.Factory {
    static final CallAdapter.Factory INSTANCE = new CompletableFutureCallAdapterFactory();

    @Override
    public @Nullable
    CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, ZHttpServiceCreator ZHttpServiceCreator) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null;
        if (getRawType(returnType) != CompletableFuture.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException("CompletableFuture return type must be parameterized" + " as CompletableFuture<Foo> or CompletableFuture<? extends Foo>");
        }
        Type innerType = getParameterUpperBound(0, (ParameterizedType) returnType);

        if (getRawType(innerType) != Response.class) {
            // Generic type is not Response<T>. Use it for body-only adapter.
            return new BodyCallAdapter<>(innerType);
        }

        // Generic type is Response<T>. Extract T and create the Response version of the adapter.
        if (!(innerType instanceof ParameterizedType)) {
            throw new IllegalStateException("Response must be parameterized" + " as Response<Foo> or Response<? extends Foo>");
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType) innerType);
        return new ResponseCallAdapter<>(responseType);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static final class BodyCallAdapter<R> implements CallAdapter<R, CompletableFuture<R>> {
        private final Type responseType;

        BodyCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public CompletableFuture<R> adapt(final Call<R> call) {
            CompletableFuture<R> future = new CallCancelCompletableFuture<>(call);
            call.enqueue(new BodyCallback(future));
            return future;
        }

        private class BodyCallback implements Callback<R> {
            private final CompletableFuture<R> future;

            public BodyCallback(CompletableFuture<R> future) {
                this.future = future;
            }

            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new HttpException(response));
                }
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static final class ResponseCallAdapter<R> implements CallAdapter<R, CompletableFuture<Response<R>>> {
        private final Type responseType;

        ResponseCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public CompletableFuture<Response<R>> adapt(final Call<R> call) {
            CompletableFuture<Response<R>> future = new CallCancelCompletableFuture<>(call);
            call.enqueue(new ResponseCallback(future));
            return future;
        }

        private class ResponseCallback implements Callback<R> {
            private final CompletableFuture<Response<R>> future;

            public ResponseCallback(CompletableFuture<Response<R>> future) {
                this.future = future;
            }

            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                future.complete(response);
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static final class CallCancelCompletableFuture<T> extends CompletableFuture<T> {
        private final Call<?> call;

        CallCancelCompletableFuture(Call<?> call) {
            this.call = call;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning) {
                call.cancel();
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
