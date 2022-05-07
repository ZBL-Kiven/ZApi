package com.zj.ok3.http;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import okhttp3.ResponseBody;

/**
 * Treat the response body on methods returning {@link ResponseBody ResponseBody} as is, i.e.
 * without converting the body to {@code byte[]}.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Streaming {}
