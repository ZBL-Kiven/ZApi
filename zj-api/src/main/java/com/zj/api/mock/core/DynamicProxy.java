package com.zj.api.mock.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@SuppressWarnings("unchecked")
public class DynamicProxy {

    public <T> T getDynamicInfo(Class<T> cls) {
        isValidate(cls);
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object obj;
                before(method);
                obj = method.invoke(proxy, args);
                after(method);
                return obj;
            }
        });
    }

    private void before(Method method) {

    }

    private void after(Method method) {


    }

    private <T> void isValidate(Class<T> cls) {
        if (!cls.isInterface() || cls.getInterfaces().length > 0)
            throw new IllegalStateException("the retrofit declared class must be an interface and no other extends yet!");
    }
}
