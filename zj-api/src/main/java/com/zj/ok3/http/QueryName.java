package com.zj.ok3.http;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Query parameter appended to the URL that has no value.
 *
 * <p>Passing a {@link java.util.List List} or array will result in a query parameter for each
 * non-{@code null} item.
 *
 * <p>Simple Example:
 *
 * <pre><code>
 * &#64;GET("/friends")
 * Call&lt;ResponseBody&gt; friends(@QueryName String filter);
 * </code></pre>
 *
 * Calling with {@code foo.friends("contains(Bob)")} yields {@code /friends?contains(Bob)}.
 *
 * <p>Array/Varargs Example:
 *
 * <pre><code>
 * &#64;GET("/friends")
 * Call&lt;ResponseBody&gt; friends(@QueryName String... filters);
 * </code></pre>
 *
 * Calling with {@code foo.friends("contains(Bob)", "age(42)")} yields {@code
 * /friends?contains(Bob)&age(42)}.
 *
 * <p>Parameter names are URL encoded by default. Specify {@link #encoded() encoded=true} to change
 * this behavior.
 *
 * <pre><code>
 * &#64;GET("/friends")
 * Call&lt;ResponseBody&gt; friends(@QueryName(encoded=true) String filter);
 * </code></pre>
 *
 * Calling with {@code foo.friends("name+age"))} yields {@code /friends?name+age}.
 *
 * @see Query
 * @see QueryMap
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface QueryName {
  /** Specifies whether the parameter is already URL encoded. */
  boolean encoded() default false;
}
