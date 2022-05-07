package com.zj.ok3.http;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.zj.ok3.Converter;

/**
 * Denotes name and value parts of a multi-part request.
 *
 * <p>Values of the map on which this annotation exists will be processed in one of two ways:
 *
 * <ul>
 *   <li>If the type is {@link okhttp3.RequestBody RequestBody} the value will be used directly with
 *       its content type.
 *   <li>Other object types will be converted to an appropriate representation by using {@linkplain
 *       Converter a converter}.
 * </ul>
 *
 * <p>
 *
 * <pre><code>
 * &#64;Multipart
 * &#64;POST("/upload")
 * Call&lt;ResponseBody&gt; upload(
 *     &#64;Part("file") RequestBody file,
 *     &#64;PartMap Map&lt;String, RequestBody&gt; params);
 * </code></pre>
 *
 * <p>A {@code null} value for the map, as a key, or as a value is not allowed.
 *
 * @see Multipart
 * @see Part
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface PartMap {
  /** The {@code Content-Transfer-Encoding} of the parts. */
  String encoding() default "binary";
}
