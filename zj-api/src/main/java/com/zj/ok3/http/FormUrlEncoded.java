package com.zj.ok3.http;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that the request body will use form URL encoding. Fields should be declared as parameters
 * and annotated with {@link Field @Field}.
 *
 * <p>Requests made with this annotation will have {@code application/x-www-form-urlencoded} MIME
 * type. Field names and values will be UTF-8 encoded before being URI-encoded in accordance to <a
 * href="http://tools.ietf.org/html/rfc3986">RFC-3986</a>.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface FormUrlEncoded {}
