package com.zj.ok3;

import static com.zj.ok3.Utils.getRawType;
import static com.zj.ok3.Utils.methodError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.Nullable;

import kotlin.coroutines.Continuation;
import okhttp3.ResponseBody;

/**
 * Adapts an invocation of an interface method into an HTTP call.
 */
@SuppressWarnings("ConstantConditions")
abstract class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {
    /**
     * Inspects the annotations on an interface method to construct a reusable service method that
     * speaks HTTP. This requires potentially-expensive reflection so it is best to build each service
     * method only once and reuse it.
     */
    static <ResponseT, ReturnT> HttpServiceMethod<ResponseT, ReturnT> parseAnnotations(ZHttpServiceCreator ZHttpServiceCreator, Method method, RequestFactory requestFactory) {
        boolean isKotlinSuspendFunction = requestFactory.isKotlinSuspendFunction;
        boolean continuationWantsResponse = false;
        boolean continuationBodyNullable = false;

        Annotation[] annotations = method.getAnnotations();
        Type adapterType;
        if (isKotlinSuspendFunction) {
            Type[] parameterTypes = method.getGenericParameterTypes();
            Type responseType = Utils.getParameterLowerBound(0, (ParameterizedType) parameterTypes[parameterTypes.length - 1]);
            if (getRawType(responseType) == Response.class && responseType instanceof ParameterizedType) {
                // Unwrap the actual body type from Response<T>.
                responseType = Utils.getParameterUpperBound(0, (ParameterizedType) responseType);
                continuationWantsResponse = true;
            }
            //else {
            // to do figure out if type is nullable or not
            // Metadata metadata = method.getDeclaringClass().getAnnotation(Metadata.class)
            // Find the entry for method
            // Determine if return type is nullable or not
            //}

            adapterType = new Utils.ParameterizedTypeImpl(null, com.zj.ok3.Call.class, responseType);
            annotations = SkipCallbackExecutorImpl.ensurePresent(annotations);
        } else {
            adapterType = method.getGenericReturnType();
        }

        com.zj.ok3.CallAdapter<ResponseT, ReturnT> callAdapter = createCallAdapter(ZHttpServiceCreator, method, adapterType, annotations);
        Type responseType = callAdapter.responseType();
        if (responseType == okhttp3.Response.class) {
            throw methodError(method, "'" + getRawType(responseType).getName() + "' is not a valid response body type. Did you mean ResponseBody?");
        }
        if (responseType == Response.class) {
            throw methodError(method, "Response must include generic type (e.g., Response<String>)");
        }
        // to do support Unit for Kotlin?
        if (requestFactory.httpMethod.equals("HEAD") && !Void.class.equals(responseType)) {
            throw methodError(method, "HEAD method must use Void as response type.");
        }

        com.zj.ok3.Converter<ResponseBody, ResponseT> responseConverter = createResponseConverter(ZHttpServiceCreator, method, responseType);

        okhttp3.Call.Factory callFactory = ZHttpServiceCreator.callFactory;
        if (!isKotlinSuspendFunction) {
            return new CallAdapted<>(requestFactory, callFactory, responseConverter, callAdapter);
        } else if (continuationWantsResponse) {
            //noinspection unchecked Kotlin compiler guarantees ReturnT to be Object.
            return (HttpServiceMethod<ResponseT, ReturnT>) new SuspendForResponse<>(requestFactory, callFactory, responseConverter, (com.zj.ok3.CallAdapter<ResponseT, com.zj.ok3.Call<ResponseT>>) callAdapter);
        } else {
            //noinspection unchecked Kotlin compiler guarantees ReturnT to be Object.
            return (HttpServiceMethod<ResponseT, ReturnT>) new SuspendForBody<>(requestFactory, callFactory, responseConverter, (com.zj.ok3.CallAdapter<ResponseT, com.zj.ok3.Call<ResponseT>>) callAdapter, continuationBodyNullable);
        }
    }

    private static <ResponseT, ReturnT> com.zj.ok3.CallAdapter<ResponseT, ReturnT> createCallAdapter(ZHttpServiceCreator ZHttpServiceCreator, Method method, Type returnType, Annotation[] annotations) {
        try {
            //noinspection unchecked
            return (com.zj.ok3.CallAdapter<ResponseT, ReturnT>) ZHttpServiceCreator.callAdapter(returnType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw methodError(method, e, "Unable to create call adapter for %s", returnType);
        }
    }

    private static <ResponseT> com.zj.ok3.Converter<ResponseBody, ResponseT> createResponseConverter(ZHttpServiceCreator ZHttpServiceCreator, Method method, Type responseType) {
        Annotation[] annotations = method.getAnnotations();
        try {
            return ZHttpServiceCreator.responseBodyConverter(responseType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw methodError(method, e, "Unable to create converter for %s", responseType);
        }
    }

    private final RequestFactory requestFactory;
    private final okhttp3.Call.Factory callFactory;
    private final com.zj.ok3.Converter<ResponseBody, ResponseT> responseConverter;

    HttpServiceMethod(RequestFactory requestFactory, okhttp3.Call.Factory callFactory, com.zj.ok3.Converter<ResponseBody, ResponseT> responseConverter) {
        this.requestFactory = requestFactory;
        this.callFactory = callFactory;
        this.responseConverter = responseConverter;
    }

    @Override
    final @Nullable
    ReturnT invoke(Object[] args) {
        com.zj.ok3.Call<ResponseT> call = new OkHttpCall<>(requestFactory, args, callFactory, responseConverter);
        return adapt(call, args);
    }

    protected abstract @Nullable
    ReturnT adapt(com.zj.ok3.Call<ResponseT> call, Object[] args);

    static final class CallAdapted<ResponseT, ReturnT> extends HttpServiceMethod<ResponseT, ReturnT> {
        private final com.zj.ok3.CallAdapter<ResponseT, ReturnT> callAdapter;

        CallAdapted(RequestFactory requestFactory, okhttp3.Call.Factory callFactory, com.zj.ok3.Converter<ResponseBody, ResponseT> responseConverter, com.zj.ok3.CallAdapter<ResponseT, ReturnT> callAdapter) {
            super(requestFactory, callFactory, responseConverter);
            this.callAdapter = callAdapter;
        }

        @Override
        protected ReturnT adapt(com.zj.ok3.Call<ResponseT> call, Object[] args) {
            return callAdapter.adapt(call);
        }
    }

    static final class SuspendForResponse<ResponseT> extends HttpServiceMethod<ResponseT, Object> {
        private final com.zj.ok3.CallAdapter<ResponseT, com.zj.ok3.Call<ResponseT>> callAdapter;

        SuspendForResponse(RequestFactory requestFactory, okhttp3.Call.Factory callFactory, com.zj.ok3.Converter<ResponseBody, ResponseT> responseConverter, com.zj.ok3.CallAdapter<ResponseT, com.zj.ok3.Call<ResponseT>> callAdapter) {
            super(requestFactory, callFactory, responseConverter);
            this.callAdapter = callAdapter;
        }

        @Override
        protected Object adapt(com.zj.ok3.Call<ResponseT> call, Object[] args) {
            call = callAdapter.adapt(call);
            //noinspection unchecked Checked by reflection inside RequestFactory.
            Continuation<Response<ResponseT>> continuation = (Continuation<Response<ResponseT>>) args[args.length - 1];
            // See SuspendForBody for explanation about this try/catch.
            try {
                return com.zj.ok3.KotlinExtensions.awaitResponse(call, continuation);
            } catch (Exception e) {
                return com.zj.ok3.KotlinExtensions.suspendAndThrow(e, continuation);
            }
        }
    }

    static final class SuspendForBody<ResponseT> extends HttpServiceMethod<ResponseT, Object> {
        private final com.zj.ok3.CallAdapter<ResponseT, com.zj.ok3.Call<ResponseT>> callAdapter;
        private final boolean isNullable;

        SuspendForBody(RequestFactory requestFactory, okhttp3.Call.Factory callFactory, Converter<ResponseBody, ResponseT> responseConverter, CallAdapter<ResponseT, com.zj.ok3.Call<ResponseT>> callAdapter, boolean isNullable) {
            super(requestFactory, callFactory, responseConverter);
            this.callAdapter = callAdapter;
            this.isNullable = isNullable;
        }

        @Override
        protected Object adapt(Call<ResponseT> call, Object[] args) {
            call = callAdapter.adapt(call);
            //noinspection unchecked Checked by reflection inside RequestFactory.
            Continuation<ResponseT> continuation = (Continuation<ResponseT>) args[args.length - 1];
            // Calls to OkHttp Call.enqueue() like those inside await and awaitNullable can sometimes
            // invoke the supplied callback with an exception before the invoking stack frame can return.
            // Coroutines will intercept the subsequent invocation of the Continuation and throw the
            // exception synchronously. A Java Proxy cannot throw checked exceptions without them being
            // declared on the interface method. To avoid the synchronous checked exception being wrapped
            // in an UndeclaredThrowableException, it is intercepted and supplied to a helper which will
            // force suspension to occur so that it can be instead delivered to the continuation to
            // bypass this restriction.
            try {
                return isNullable ? KotlinExtensions.awaitNullable(call, continuation) : KotlinExtensions.await(call, continuation);
            } catch (Exception e) {
                return KotlinExtensions.suspendAndThrow(e, continuation);
            }
        }
    }
}
