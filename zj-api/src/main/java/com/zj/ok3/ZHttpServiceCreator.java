package com.zj.ok3;

import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import com.zj.ok3.http.GET;
import com.zj.ok3.http.HTTP;
import com.zj.ok3.http.Header;
import com.zj.ok3.http.Url;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public final class ZHttpServiceCreator {
    private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();

    final okhttp3.Call.Factory callFactory;
    final HttpUrl baseUrl;
    final List<com.zj.ok3.Converter.Factory> converterFactories;
    final List<com.zj.ok3.CallAdapter.Factory> callAdapterFactories;
    final @Nullable
    Executor callbackExecutor;
    final boolean validateEagerly;

    ZHttpServiceCreator(okhttp3.Call.Factory callFactory, HttpUrl baseUrl, List<com.zj.ok3.Converter.Factory> converterFactories, List<com.zj.ok3.CallAdapter.Factory> callAdapterFactories, @Nullable Executor callbackExecutor, boolean validateEagerly) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.converterFactories = converterFactories; // Copy+unmodifiable at call site.
        this.callAdapterFactories = callAdapterFactories; // Copy+unmodifiable at call site.
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     *
     * <p>The relative path for a given method is obtained from an annotation on the method describing
     * the request type. The built-in methods are {@link GET GET}, {@link
     * com.zj.ok3.http.PUT PUT}, {@link com.zj.ok3.http.POST POST}, {@link com.zj.ok3.http.PATCH PATCH},
     * {@link com.zj.ok3.http.HEAD HEAD}, {@link com.zj.ok3.http.DELETE DELETE} and {@link
     * com.zj.ok3.http.OPTIONS OPTIONS}. You can use a custom HTTP method with {@link HTTP @HTTP}. For
     * a dynamic URL, omit the path on the annotation and annotate the first parameter with {@link
     * Url @Url}.
     *
     * <p>Method parameters can be used to replace parts of the URL by annotating them with {@link
     * com.zj.ok3.http.Path @Path}. Replacement sections are denoted by an identifier surrounded by
     * curly braces (e.g., "{foo}"). To add items to the query string of a URL use {@link
     * com.zj.ok3.http.Query @Query}.
     *
     * <p>The body of a request is denoted by the {@link com.zj.ok3.http.Body @Body} annotation. The
     * object will be converted to request representation by one of the {@link com.zj.ok3.Converter.Factory}
     * instances. A {@link RequestBody} can also be used for a raw representation.
     *
     * <p>Alternative request body formats are supported by method annotations and corresponding
     * parameter annotations:
     *
     * <ul>
     *   <li>{@link com.zj.ok3.http.FormUrlEncoded @FormUrlEncoded} - Form-encoded data with key-value
     *       pairs specified by the {@link com.zj.ok3.http.Field @Field} parameter annotation.
     *   <li>{@link com.zj.ok3.http.Multipart @Multipart} - RFC 2388-compliant multipart data with
     *       parts specified by the {@link com.zj.ok3.http.Part @Part} parameter annotation.
     * </ul>
     *
     * <p>Additional static headers can be added for an endpoint using the {@link
     * com.zj.ok3.http.Headers @Headers} method annotation. For per-request control over a header
     * annotate a parameter with {@link Header @Header}.
     *
     * <p>By default, methods return a {@link com.zj.ok3.Call} which represents the HTTP request. The generic
     * parameter of the call is the response body type and will be converted by one of the {@link
     * com.zj.ok3.Converter.Factory} instances. {@link ResponseBody} can also be used for a raw representation.
     * {@link Void} can be used if you do not care about the body contents.
     */
    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(final Class<T> service, MethodHandler methodHandler) {
        validateServiceInterface(service, methodHandler);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new InvocationHandler() {
            private final Platform platform = Platform.get();
            private final Object[] emptyArgs = new Object[0];

            @Override
            public @Nullable
            Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
                // If the method is a method from Object then defer to normal invocation.
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                args = args != null ? args : emptyArgs;
                if (platform.isDefaultMethod(method)) {
                    return platform.invokeDefaultMethod(method, service, proxy, args);
                } else {
                    methodHandler.parseParameterMaps(method, args);
                    return loadServiceMethod(method, methodHandler).invoke(args);
                }
            }
        });
    }

    private void validateServiceInterface(Class<?> service, MethodHandler methodHandler) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        Deque<Class<?>> check = new ArrayDeque<>(1);
        check.add(service);
        while (!check.isEmpty()) {
            Class<?> candidate = check.removeFirst();
            if (candidate.getTypeParameters().length != 0) {
                StringBuilder message = new StringBuilder("Type parameters are unsupported on ").append(candidate.getName());
                if (candidate != service) {
                    message.append(" which is an interface of ").append(service.getName());
                }
                throw new IllegalArgumentException(message.toString());
            }
            Collections.addAll(check, candidate.getInterfaces());
        }

        if (validateEagerly) {
            Platform platform = Platform.get();
            for (Method method : service.getDeclaredMethods()) {
                if (!platform.isDefaultMethod(method) && !Modifier.isStatic(method.getModifiers())) {
                    loadServiceMethod(method, methodHandler);
                }
            }
        }
    }

    ServiceMethod<?> loadServiceMethod(Method method, MethodHandler methodHandler) {
        ServiceMethod<?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = ServiceMethod.parseAnnotations(this, method, methodHandler);
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    /**
     * The factory used to create {@linkplain okhttp3.Call OkHttp calls} for sending a HTTP requests.
     * Typically an instance of {@link OkHttpClient}.
     */
    public okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    /**
     * The API base URL.
     */
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    /**
     * Returns a list of the factories tried when creating a {@linkplain #callAdapter(Type,
     * Annotation[])} call adapter}.
     */
    public List<com.zj.ok3.CallAdapter.Factory> callAdapterFactories() {
        return callAdapterFactories;
    }

    /**
     * Returns the {@link com.zj.ok3.CallAdapter} for {@code returnType} from the available {@linkplain
     * #callAdapterFactories() factories}.
     *
     * @throws IllegalArgumentException if no call adapter available for {@code type}.
     */
    public com.zj.ok3.CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }

    /**
     * Returns the {@link com.zj.ok3.CallAdapter} for {@code returnType} from the available {@linkplain
     * #callAdapterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no call adapter available for {@code type}.
     */
    public com.zj.ok3.CallAdapter<?, ?> nextCallAdapter(@Nullable com.zj.ok3.CallAdapter.Factory skipPast, Type returnType, Annotation[] annotations) {
        Objects.requireNonNull(returnType, "returnType == null");
        Objects.requireNonNull(annotations, "annotations == null");

        int start = callAdapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            com.zj.ok3.CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate call adapter for ").append(returnType).append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns an unmodifiable list of the factories tried when creating a {@linkplain
     * #requestBodyConverter(Type, Annotation[], Annotation[]) request body converter}, a {@linkplain
     * #responseBodyConverter(Type, Annotation[]) response body converter}, or a {@linkplain
     * #stringConverter(Type, Annotation[]) string converter}.
     */
    public List<com.zj.ok3.Converter.Factory> converterFactories() {
        return converterFactories;
    }

    /**
     * Returns a {@link com.zj.ok3.Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> com.zj.ok3.Converter<T, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        return nextRequestBodyConverter(null, type, parameterAnnotations, methodAnnotations);
    }

    /**
     * Returns a {@link com.zj.ok3.Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> com.zj.ok3.Converter<T, RequestBody> nextRequestBodyConverter(@Nullable com.zj.ok3.Converter.Factory skipPast, Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        Objects.requireNonNull(type, "type == null");
        Objects.requireNonNull(parameterAnnotations, "parameterAnnotations == null");
        Objects.requireNonNull(methodAnnotations, "methodAnnotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            com.zj.ok3.Converter.Factory factory = converterFactories.get(i);
            com.zj.ok3.Converter<?, RequestBody> converter = factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (com.zj.ok3.Converter<T, RequestBody>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ").append(type).append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link com.zj.ok3.Converter} for {@link ResponseBody} to {@code type} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> com.zj.ok3.Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        return nextResponseBodyConverter(null, type, annotations);
    }

    /**
     * Returns a {@link com.zj.ok3.Converter} for {@link ResponseBody} to {@code type} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> com.zj.ok3.Converter<ResponseBody, T> nextResponseBodyConverter(@Nullable com.zj.ok3.Converter.Factory skipPast, Type type, Annotation[] annotations) {
        Objects.requireNonNull(type, "type == null");
        Objects.requireNonNull(annotations, "annotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            com.zj.ok3.Converter<ResponseBody, ?> converter = converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (com.zj.ok3.Converter<ResponseBody, T>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ").append(type).append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link com.zj.ok3.Converter} for {@code type} to {@link String} from the available {@linkplain
     * #converterFactories() factories}.
     */
    public <T> com.zj.ok3.Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        Objects.requireNonNull(type, "type == null");
        Objects.requireNonNull(annotations, "annotations == null");

        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            com.zj.ok3.Converter<?, String> converter = converterFactories.get(i).stringConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (com.zj.ok3.Converter<T, String>) converter;
            }
        }

        // Nothing matched. Resort to default converter which just calls toString().
        //noinspection unchecked
        return (com.zj.ok3.Converter<T, String>) com.zj.ok3.BuiltInConverters.ToStringConverter.INSTANCE;
    }

    /**
     * The executor used for {@link com.zj.ok3.Callback} methods on a {@link com.zj.ok3.Call}. This may be {@code null}, in
     * which case callbacks should be made synchronously on the background thread.
     */
    public @Nullable
    Executor callbackExecutor() {
        return callbackExecutor;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Build a new {@link ZHttpServiceCreator}.
     *
     * <p>Calling {@link #baseUrl} is required before calling {@link #build()}. All other methods are
     * optional.
     */
    public static final class Builder {
        private final Platform platform;
        private @Nullable
        okhttp3.Call.Factory callFactory;
        private @Nullable
        HttpUrl baseUrl;
        private final List<com.zj.ok3.Converter.Factory> converterFactories = new ArrayList<>();
        private final List<com.zj.ok3.CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        private @Nullable
        Executor callbackExecutor;
        private boolean validateEagerly;

        Builder(Platform platform) {
            this.platform = platform;
        }

        public Builder() {
            this(Platform.get());
        }

        Builder(ZHttpServiceCreator ZHttpServiceCreator) {
            platform = Platform.get();
            callFactory = ZHttpServiceCreator.callFactory;
            baseUrl = ZHttpServiceCreator.baseUrl;

            // Do not add the default BuiltIntConverters and platform-aware converters added by build().
            for (int i = 1, size = ZHttpServiceCreator.converterFactories.size() - platform.defaultConverterFactoriesSize(); i < size; i++) {
                converterFactories.add(ZHttpServiceCreator.converterFactories.get(i));
            }

            // Do not add the default, platform-aware call adapters added by build().
            for (int i = 0, size = ZHttpServiceCreator.callAdapterFactories.size() - platform.defaultCallAdapterFactoriesSize(); i < size; i++) {
                callAdapterFactories.add(ZHttpServiceCreator.callAdapterFactories.get(i));
            }

            callbackExecutor = ZHttpServiceCreator.callbackExecutor;
            validateEagerly = ZHttpServiceCreator.validateEagerly;
        }

        /**
         * The HTTP client used for requests.
         *
         * <p>This is a convenience method for calling {@link #callFactory}.
         */
        public Builder client(OkHttpClient client) {
            return callFactory(Objects.requireNonNull(client, "client == null"));
        }

        /**
         * Specify a custom call factory for creating {@link com.zj.ok3.Call} instances.
         *
         * <p>Note: Calling {@link #client} automatically sets this value.
         */
        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory = Objects.requireNonNull(factory, "factory == null");
            return this;
        }

        /**
         * Set the API base URL.
         *
         * @see #baseUrl(HttpUrl)
         */
        public Builder baseUrl(URL baseUrl) {
            Objects.requireNonNull(baseUrl, "baseUrl == null");
            return baseUrl(HttpUrl.get(baseUrl.toString()));
        }

        /**
         * Set the API base URL.
         *
         * @see #baseUrl(HttpUrl)
         */
        public Builder baseUrl(String baseUrl) {
            Objects.requireNonNull(baseUrl, "baseUrl == null");
            return baseUrl(HttpUrl.get(baseUrl));
        }

        /**
         * Set the API base URL.
         *
         * <p>The specified endpoint values (such as with {@link GET @GET}) are resolved against this
         * value using {@link HttpUrl#resolve(String)}. The behavior of this matches that of an {@code
         * <a href="">} link on a website resolving on the current URL.
         *
         * <p><b>Base URLs should always end in {@code /}.</b>
         *
         * <p>A trailing {@code /} ensures that endpoints values which are relative paths will correctly
         * append themselves to a base which has path components.
         *
         * <p><b>Correct:</b><br>
         * Base URL: http://example.com/api/<br>
         * Endpoint: foo/bar/<br>
         * Result: http://example.com/api/foo/bar/
         *
         * <p><b>Incorrect:</b><br>
         * Base URL: http://example.com/api<br>
         * Endpoint: foo/bar/<br>
         * Result: http://example.com/foo/bar/
         *
         * <p>This method enforces that {@code baseUrl} has a trailing {@code /}.
         *
         * <p><b>Endpoint values which contain a leading {@code /} are absolute.</b>
         *
         * <p>Absolute values retain only the host from {@code baseUrl} and ignore any specified path
         * components.
         *
         * <p>Base URL: http://example.com/api/<br>
         * Endpoint: /foo/bar/<br>
         * Result: http://example.com/foo/bar/
         *
         * <p>Base URL: http://example.com/<br>
         * Endpoint: /foo/bar/<br>
         * Result: http://example.com/foo/bar/
         *
         * <p><b>Endpoint values may be a full URL.</b>
         *
         * <p>Values which have a host replace the host of {@code baseUrl} and values also with a scheme
         * replace the scheme of {@code baseUrl}.
         *
         * <p>Base URL: http://example.com/<br>
         * Endpoint: https://github.com/square/hsc/<br>
         * Result: https://github.com/square/hsc/
         *
         * <p>Base URL: http://example.com<br>
         * Endpoint: //github.com/square/hsc/<br>
         * Result: http://github.com/square/hsc/ (note the scheme stays 'http')
         */
        public Builder baseUrl(HttpUrl baseUrl) {
            Objects.requireNonNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Add converter factory for serialization and deserialization of objects.
         */
        public Builder addConverterFactory(com.zj.ok3.Converter.Factory factory) {
            converterFactories.add(Objects.requireNonNull(factory, "factory == null"));
            return this;
        }

        /**
         * Add a call adapter factory for supporting service method return types other than {@link
         * com.zj.ok3.Call}.
         */
        public Builder addCallAdapterFactory(com.zj.ok3.CallAdapter.Factory factory) {
            callAdapterFactories.add(Objects.requireNonNull(factory, "factory == null"));
            return this;
        }

        /**
         * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
         * your service method.
         *
         * <p>Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
         * return types}.
         */
        public Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = Objects.requireNonNull(executor, "executor == null");
            return this;
        }

        /**
         * Returns a modifiable list of call adapter factories.
         */
        public List<com.zj.ok3.CallAdapter.Factory> callAdapterFactories() {
            return this.callAdapterFactories;
        }

        /**
         * Returns a modifiable list of converter factories.
         */
        public List<com.zj.ok3.Converter.Factory> converterFactories() {
            return this.converterFactories;
        }

        /**
         * When calling {@link #create} on the resulting {@link ZHttpServiceCreator} instance, eagerly validate the
         * configuration of all methods in the supplied interface.
         */
        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        /**
         * Create the {@link ZHttpServiceCreator} instance using the configured values.
         *
         * <p>Note: If neither {@link #client} nor {@link #callFactory} is called a default {@link
         * OkHttpClient} will be created and used.
         */
        public ZHttpServiceCreator build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            Executor callbackExecutor = this.callbackExecutor;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }

            // Make a defensive copy of the adapters and add the default Call adapter.
            List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
            callAdapterFactories.addAll(platform.defaultCallAdapterFactories(callbackExecutor));

            // Make a defensive copy of the converters.
            List<Converter.Factory> converterFactories = new ArrayList<>(1 + this.converterFactories.size() + platform.defaultConverterFactoriesSize());

            // Add the built-in converter factory first. This prevents overriding its behavior but also
            // ensures correct behavior when using converters that consume all types.
            converterFactories.add(new com.zj.ok3.BuiltInConverters());
            converterFactories.addAll(this.converterFactories);
            converterFactories.addAll(platform.defaultConverterFactories());

            return new ZHttpServiceCreator(callFactory, baseUrl, unmodifiableList(converterFactories), unmodifiableList(callAdapterFactories), callbackExecutor, validateEagerly);
        }
    }
}
