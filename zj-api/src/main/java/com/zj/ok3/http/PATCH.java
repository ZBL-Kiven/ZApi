package com.zj.ok3.http;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zj.ok3.ZHttpServiceCreator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import okhttp3.HttpUrl;

/** Make a PATCH request. */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface PATCH {
  /**
   * A relative or absolute path, or full URL of the endpoint. This value is optional if the first
   * parameter of the method is annotated with {@link Url @Url}.
   *
   * <p>See {@linkplain ZHttpServiceCreator.Builder#baseUrl(HttpUrl) base URL} for details of how
   * this is resolved against a base URL to create the full endpoint URL.
   */
  String value() default "";
}
