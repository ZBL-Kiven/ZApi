package com.zj.ok3;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.Nullable;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

abstract class ParameterHandler<T> {

    final MethodHandler handler;

    ParameterHandler(MethodHandler handler) {
        this.handler = handler;
    }

    abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;

    final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>(handler) {
            @Override
            void apply(RequestBuilder builder, @Nullable Iterable<T> values) throws IOException {
                if (values == null) return; // Skip null values.

                for (T value : values) {
                    ParameterHandler.this.apply(builder, value);
                }
            }
        };
    }

    final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>(handler) {
            @Override
            void apply(RequestBuilder builder, @Nullable Object values) throws IOException {
                if (values == null) return; // Skip null values.

                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    //noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }

    static final class RelativeUrl extends ParameterHandler<Object> {
        private final Method method;
        private final int p;

        RelativeUrl(Method method, int p, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Object value) {
            if (value == null) {
                throw Utils.parameterError(method, p, "@Url parameter is null.");
            }
            builder.setRelativeUrl(value);
        }
    }

    static final class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final com.zj.ok3.Converter<T, String> valueConverter;

        Header(String name, com.zj.ok3.Converter<T, String> valueConverter, MethodHandler handler) {
            super(handler);
            this.name = Objects.requireNonNull(name, "name == null");
            this.valueConverter = valueConverter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.
            String headerValue = valueConverter.convert(value);
            if (headerValue == null) return; // Skip converted but null values.
            builder.addHeader(name, headerValue);
            handler.onRequestParams(name, value);
        }
    }

    static final class Path<T> extends ParameterHandler<T> {
        private final Method method;
        private final int p;
        private final String name;
        private final com.zj.ok3.Converter<T, String> valueConverter;
        private final boolean encoded;

        Path(Method method, int p, String name, com.zj.ok3.Converter<T, String> valueConverter, boolean encoded, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.name = Objects.requireNonNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Path parameter \"" + name + "\" value must not be null.");
            }
            builder.addPathParam(name, valueConverter.convert(value), encoded);
            this.handler.onRequestParams(this.name, value);
        }
    }

    static final class Query<T> extends ParameterHandler<T> {
        private final String name;
        private final com.zj.ok3.Converter<T, String> valueConverter;
        private final boolean encoded;

        Query(String name, com.zj.ok3.Converter<T, String> valueConverter, boolean encoded, MethodHandler handler) {
            super(handler);
            this.name = Objects.requireNonNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.
            String queryValue = valueConverter.convert(value);
            if (queryValue == null) return; // Skip converted but null values
            builder.addQueryParam(name, queryValue, encoded);
            handler.onRequestParams(name, value);
        }
    }

    static final class QueryName<T> extends ParameterHandler<T> {
        private final com.zj.ok3.Converter<T, String> nameConverter;
        private final boolean encoded;

        QueryName(com.zj.ok3.Converter<T, String> nameConverter, boolean encoded, MethodHandler handler) {
            super(handler);
            this.nameConverter = nameConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return;
            String name = nameConverter.convert(value);
            builder.addQueryParam(name, null, encoded);
            if (name != null) handler.onRequestParams(name, encoded);
        }
    }

    static final class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final com.zj.ok3.Converter<T, String> valueConverter;
        private final boolean encoded;

        QueryMap(Method method, int p, com.zj.ok3.Converter<T, String> valueConverter, boolean encoded, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Query map was null");
            }
            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw Utils.parameterError(method, p, "Query map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw Utils.parameterError(method, p, "Query map contained null value for key '" + entryKey + "'.");
                }

                String convertedEntryValue = valueConverter.convert(entryValue);
                if (convertedEntryValue == null) {
                    throw Utils.parameterError(method, p, "Query map value '" + entryValue + "' converted to null by " + valueConverter.getClass().getName() + " for key '" + entryKey + "'.");
                }
                builder.addQueryParam(entryKey, convertedEntryValue, encoded);
                handler.onRequestParams(entryKey, entryValue);
            }
        }
    }

    static final class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final com.zj.ok3.Converter<T, String> valueConverter;

        HeaderMap(Method method, int p, com.zj.ok3.Converter<T, String> valueConverter, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Header map was null.");
            }
            for (Map.Entry<String, T> entry : value.entrySet()) {
                String headerName = entry.getKey();
                if (headerName == null) {
                    throw Utils.parameterError(method, p, "Header map contained null key.");
                }
                T headerValue = entry.getValue();
                if (headerValue == null) {
                    throw Utils.parameterError(method, p, "Header map contained null value for key '" + headerName + "'.");
                }
                builder.addHeader(headerName, valueConverter.convert(headerValue));
                handler.onRequestParams(headerName, headerValue);
            }
        }
    }

    static final class Headers extends ParameterHandler<okhttp3.Headers> {
        private final Method method;
        private final int p;

        Headers(Method method, int p, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable okhttp3.Headers headers) {
            if (headers == null) {
                throw Utils.parameterError(method, p, "Headers parameter must not be null.");
            }
            builder.addHeaders(headers);
            Map<String, List<String>> map = headers.toMultimap();
            for (Map.Entry<String, List<String>> e : map.entrySet()) {
                int a = -1;
                for (String s : e.getValue()) {
                    a++;
                    handler.onRequestParams(e.getKey() + (a == 0 ? "" : a), s);
                }
            }
        }
    }

    static final class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final com.zj.ok3.Converter<T, String> valueConverter;
        private final boolean encoded;

        Field(String name, com.zj.ok3.Converter<T, String> valueConverter, boolean encoded, MethodHandler handler) {
            super(handler);
            this.name = Objects.requireNonNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.
            String fieldValue = valueConverter.convert(value);
            if (fieldValue == null) return; // Skip null converted values
            builder.addFormField(name, fieldValue, encoded);
            handler.onRequestParams(name, value);
        }
    }

    static final class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final com.zj.ok3.Converter<T, String> valueConverter;
        private final boolean encoded;

        FieldMap(Method method, int p, com.zj.ok3.Converter<T, String> valueConverter, boolean encoded, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Field map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw Utils.parameterError(method, p, "Field map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw Utils.parameterError(method, p, "Field map contained null value for key '" + entryKey + "'.");
                }

                String fieldEntry = valueConverter.convert(entryValue);
                if (fieldEntry == null) {
                    throw Utils.parameterError(method, p, "Field map value '" + entryValue + "' converted to null by " + valueConverter.getClass().getName() + " for key '" + entryKey + "'.");
                }
                builder.addFormField(entryKey, fieldEntry, encoded);
                handler.onRequestParams(entryKey, entryValue);
            }
        }
    }

    static final class Part<T> extends ParameterHandler<T> {
        private final Method method;
        private final int p;
        private final okhttp3.Headers headers;
        private final com.zj.ok3.Converter<T, RequestBody> converter;

        Part(Method method, int p, okhttp3.Headers headers, com.zj.ok3.Converter<T, RequestBody> converter, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.headers = headers;
            this.converter = converter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) return; // Skip null values.
            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw Utils.parameterError(method, p, "Unable to convert " + value + " to RequestBody", e);
            }
            builder.addPart(headers, body);
            Map<String, List<String>> map = headers.toMultimap();
            for (Map.Entry<String, List<String>> e : map.entrySet()) {
                handler.onRequestParams(e.getKey(), value);
            }
        }
    }

    static final class RawPart extends ParameterHandler<MultipartBody.Part> {

        RawPart(MethodHandler handler) {
            super(handler);
        }

        @Override
        void apply(RequestBuilder builder, @Nullable MultipartBody.Part value) {
            if (value != null) { // Skip null values.
                builder.addPart(value);
            }
        }
    }

    static final class PartMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final com.zj.ok3.Converter<T, RequestBody> valueConverter;
        private final String transferEncoding;

        PartMap(Method method, int p, com.zj.ok3.Converter<T, RequestBody> valueConverter, String transferEncoding, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.transferEncoding = transferEncoding;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Part map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw Utils.parameterError(method, p, "Part map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw Utils.parameterError(method, p, "Part map contained null value for key '" + entryKey + "'.");
                }
                okhttp3.Headers headers = okhttp3.Headers.of("Content-Disposition", "form-data; name=\"" + entryKey + "\"", "Content-Transfer-Encoding", transferEncoding);
                builder.addPart(headers, valueConverter.convert(entryValue));
                handler.onRequestParams(entryKey, entryValue);
            }
        }
    }

    static final class Body<T> extends ParameterHandler<T> {
        private final Method method;
        private final int p;
        private final com.zj.ok3.Converter<T, RequestBody> converter;

        Body(Method method, int p, Converter<T, RequestBody> converter, MethodHandler handler) {
            super(handler);
            this.method = method;
            this.p = p;
            this.converter = converter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) {
                throw Utils.parameterError(method, p, "Body parameter value must not be null.");
            }
            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw Utils.parameterError(method, e, p, "Unable to convert " + value + " to RequestBody");
            }
            builder.setBody(body);
        }
    }

    static final class Tag<T> extends ParameterHandler<T> {
        final Class<T> cls;

        Tag(Class<T> cls, MethodHandler handler) {
            super(handler);
            this.cls = cls;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            builder.addTag(cls, value);
        }
    }
}
