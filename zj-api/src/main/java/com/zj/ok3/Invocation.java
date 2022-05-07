package com.zj.ok3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A single invocation of a ZHttpServiceCreator service interface method. This class captures both the method
 * that was called and the arguments to the method.
 *
 * <p>ZHttpServiceCreator automatically adds an invocation to each OkHttp request as a tag. You can retrieve
 * the invocation in an OkHttp interceptor for metrics and monitoring.
 *
 * <pre><code>
 * class InvocationLogger implements Interceptor {
 *   &#64;Override public Response intercept(Chain chain) throws IOException {
 *     Request request = chain.request();
 *     Invocation invocation = request.tag(Invocation.class);
 *     if (invocation != null) {
 *       System.out.printf("%s.%s %s%n",
 *           invocation.method().getDeclaringClass().getSimpleName(),
 *           invocation.method().getName(), invocation.arguments());
 *     }
 *     return chain.proceed(request);
 *   }
 * }
 * </code></pre>
 *
 * <strong>Note:</strong> use caution when examining an invocation's arguments. Although the
 * arguments list is unmodifiable, the arguments themselves may be mutable. They may also be unsafe
 * for concurrent access. For best results declare ZHttpServiceCreator service interfaces using only immutable
 * types for parameters!
 */
public final class Invocation {
    public static Invocation of(Method method, List<?> arguments) {
        Objects.requireNonNull(method, "method == null");
        Objects.requireNonNull(arguments, "arguments == null");
        return new Invocation(method, new ArrayList<>(arguments)); // Defensive copy.
    }

    private final Method method;
    private final List<?> arguments;

    /**
     * Trusted constructor assumes ownership of {@code arguments}.
     */
    Invocation(Method method, List<?> arguments) {
        this.method = method;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public Method method() {
        return method;
    }

    public List<?> arguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return String.format("%s.%s() %s", method.getDeclaringClass().getName(), method.getName(), arguments);
    }
}
