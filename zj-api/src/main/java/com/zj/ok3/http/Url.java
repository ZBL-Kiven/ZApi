package com.zj.ok3.http;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import okhttp3.HttpUrl;


/**
 * URL resolved against the {@linkplain ZHttpServiceCreator#baseUrl() base URL}.
 *
 * <pre><code>
 * &#64;GET
 * Call&lt;ResponseBody&gt; list(@Url String url);
 * </code></pre>
 *
 * <p>See {@linkplain ZHttpServiceCreator.Builder#baseUrl(HttpUrl) base URL} for details of how the
 * value will be resolved against a base URL to create the full endpoint URL.
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Url {}
