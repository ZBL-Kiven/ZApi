package com.zj.ok3;

import static com.zj.ok3.Utils.methodError;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import androidx.annotation.Nullable;

abstract class ServiceMethod<T> {
    static <T> ServiceMethod<T> parseAnnotations(ZHttpServiceCreator ZHttpServiceCreator, Method method, MethodHandler methodHandler) {
        RequestFactory requestFactory = RequestFactory.parseAnnotations(ZHttpServiceCreator, method, methodHandler);

        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw methodError(method, "Method return type must not include a type variable or wildcard: %s", returnType);
        }
        if (returnType == void.class) {
            throw methodError(method, "Service methods cannot return void.");
        }
        return com.zj.ok3.HttpServiceMethod.parseAnnotations(ZHttpServiceCreator, method, requestFactory);
    }

    abstract @Nullable
    T invoke(Object[] args);
}
