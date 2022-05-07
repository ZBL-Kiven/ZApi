package com.zj.ok3.http;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.zj.ok3.Converter;


/**
 * Use this annotation on a service method param when you want to directly control the request body
 * of a POST/PUT request (instead of sending in as request parameters or form-style request body).
 * The object will be serialized using the {@link com.zj.ok3.ZHttpServiceCreator} instance {@link Converter
 * Converter} and the result will be set directly as the request body.
 *
 * <p>Body parameters may not be {@code null}.
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Body {}
