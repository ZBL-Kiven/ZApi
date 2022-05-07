package com.zj.ok3;

import android.os.Build;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import androidx.annotation.Nullable;

import okhttp3.ResponseBody;

// Only added when Optional is available (Java 8+ / Android API 24+).
final class OptionalConverterFactory extends com.zj.ok3.Converter.Factory {
    static final com.zj.ok3.Converter.Factory INSTANCE = new OptionalConverterFactory();

    @Override
    public @Nullable
    com.zj.ok3.Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, ZHttpServiceCreator ZHttpServiceCreator) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N || getRawType(type) != Optional.class) {
            return null;
        }

        Type innerType = getParameterUpperBound(0, (ParameterizedType) type);
        com.zj.ok3.Converter<ResponseBody, Object> delegate = ZHttpServiceCreator.responseBodyConverter(innerType, annotations);
        return new OptionalConverter<>(delegate);
    }

    static final class OptionalConverter<T> implements com.zj.ok3.Converter<ResponseBody, Optional<T>> {
        final com.zj.ok3.Converter<ResponseBody, T> delegate;

        OptionalConverter(Converter<ResponseBody, T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<T> convert(ResponseBody value) throws IOException {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) throw new IllegalArgumentException();
            return Optional.ofNullable(delegate.convert(value));
        }
    }
}
