package com.zj.ok3;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Request;
import okio.Timeout;

final class DefaultCallAdapterFactory extends CallAdapter.Factory {
    private final @Nullable
    Executor callbackExecutor;

    DefaultCallAdapterFactory(@Nullable Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public @Nullable
    com.zj.ok3.CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, ZHttpServiceCreator ZHttpServiceCreator) {
        if (getRawType(returnType) != com.zj.ok3.Call.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
        final Type responseType = Utils.getParameterUpperBound(0, (ParameterizedType) returnType);

        final Executor executor = Utils.isAnnotationPresent(annotations, SkipCallbackExecutor.class) ? null : callbackExecutor;

        return new CallAdapter<Object, com.zj.ok3.Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public com.zj.ok3.Call<Object> adapt(com.zj.ok3.Call<Object> call) {
                return executor == null ? call : new ExecutorCallbackCall<>(executor, call);
            }
        };
    }

    static final class ExecutorCallbackCall<T> implements com.zj.ok3.Call<T> {
        final Executor callbackExecutor;
        final com.zj.ok3.Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, com.zj.ok3.Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(final com.zj.ok3.Callback<T> callback) {
            Objects.requireNonNull(callback, "callback == null");

            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(com.zj.ok3.Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(() -> {
                        if (delegate.isCanceled()) {
                            // Emulate OkHttp's behavior of throwing/delivering an IOException on
                            // cancellation.
                            callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                        } else {
                            callback.onResponse(ExecutorCallbackCall.this, response);
                        }
                    });
                }

                @Override
                public void onFailure(com.zj.ok3.Call<T> call, final Throwable t) {
                    callbackExecutor.execute(() -> callback.onFailure(ExecutorCallbackCall.this, t));
                }
            });
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        // Performing deep clone.
        @NonNull
        @SuppressWarnings({"MethodDoesntCallSuperMethod"})
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }

        @Override
        public Timeout timeout() {
            return delegate.timeout();
        }
    }
}
