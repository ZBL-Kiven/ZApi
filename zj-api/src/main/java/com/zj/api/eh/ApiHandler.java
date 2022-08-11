package com.zj.api.eh;

import static com.zj.api.ZApi.mBaseReadTimeoutMills;
import static com.zj.api.ZApi.mBaseTimeoutMills;
import static com.zj.api.ZApi.mBaseWriteTimeoutMills;

import com.zj.api.exception.ApiException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kotlin.coroutines.Continuation;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiHandler {

    /**
     * If you had set ErrorHandler, this annotation indicates what thread you want
     * {@link com.zj.api.eh.ErrorHandler#interruptSuccessBody(String, int, Object, EHParam, Continuation)} (String, int, Object, Continuation)} to run on.
     * The thread you choose only takes effect in the EH scope.
     */
    @LimitScope String successEHScope() default "";

    /**
     * If you had set ErrorHandler, this annotation indicates what thread you want
     * {@link com.zj.api.eh.ErrorHandler#interruptErrorBody(ApiException, EHParam, Continuation)} (ApiException, Continuation)}  to run on.
     * The thread you choose only takes effect in the EH scope.
     */
    @LimitScope String errorEHScope() default "";

    /**
     * Marks an execute function that will be available in ErrorHandler and any subsequent results,
     * usually to handle some special network access.
     *
     * @see ApiException#getId()
     */
    String id() default "";

    /**
     * Set a unique special timeout for a single execution func,
     * this setting will override the wider timeout set by {@link com.zj.api.base.BaseApiProxy#timeOut(long)}.
     * It will still work with ErrorHandler's error handling timeout.
     */
    int timeOut() default mBaseTimeoutMills;

    int readTimeOut() default mBaseReadTimeoutMills;

    int writeTimeOut() default mBaseWriteTimeoutMills;
}
