@file:Suppress("unused")

package com.zj.api.base

import com.zj.api.ZApi.Companion.mBaseTimeoutMills
import com.zj.api.interfaces.ApiFactory
import com.zj.api.eh.ErrorHandler
import com.zj.api.interceptor.*
import com.zj.api.utils.LogUtils
import java.io.InputStream

@Suppress("MemberVisibilityCanBePrivate")
class BaseApiProxy<T : Any, ERROR_HANDLER : ErrorHandler>(private val clazz: Class<T>, private val handler: ERROR_HANDLER? = null) {

    private var timeOut: Long = mBaseTimeoutMills
    private var header: HeaderProvider? = null
    private var baseUrl: UrlProvider? = null
    private var debugAble: Boolean = true
    private var mockAble: Boolean = true
    private var certificate: Array<InputStream>? = null
    private var logLevel = LogLevel.NONE.with
    private var logLevelSetting = false

    /**
     * set request certificate
     * */
    fun certificate(certificate: Array<InputStream>): BaseApiProxy<T, ERROR_HANDLER> {
        this.certificate = certificate
        return this
    }

    fun baseUrl(url: UrlProvider): BaseApiProxy<T, ERROR_HANDLER> {
        this.baseUrl = url
        return this
    }

    fun header(header: HeaderProvider?): BaseApiProxy<T, ERROR_HANDLER> {
        this.header = header
        return this
    }

    /**
     * set a timeout for this ZApi builder , for all method created by this time in service.
     * Ignore at , if you've presented @annotation[com.zj.api.eh.ApiHandler] and sat the timeout.
     *
     * The ErrorHandlerTimeOut property set here will take effect on all services containing ErrorHandler called by the initialized ZApi instance this time.
     * @param timeOut When the service method fails, it will call back to ErrorHandler (if set),
     * and wait for ErrorHandler to process and return the interception change result , if the waiting time is longer than this time,
     * it is considered that the error processing has timed out, at this time,
     * the subsequent processing steps of ErrorHandler will be ignored, and the error will be returned directly to the calling thread.
     * @see [ErrorHandler.interruptErrorBody]
     * */
    fun timeOut(timeOut: Long): BaseApiProxy<T, ERROR_HANDLER> {
        this.timeOut = timeOut
        return this
    }

    /**
     * Prerequisites the debugMod [debugAble] is allowed.
     * set of the debug level of http logger , see [LogLevel]
     * the level : NONE will clear the http logs.
     * you can plus another one for merge. example SERVER_HEADERS + HEADERS + BODY
     * @Default HEADERS & BASIC & RESULT_BODY
     * */
    fun logLevel(level: LogLevel): BaseApiProxy<T, ERROR_HANDLER> {
        logLevelSetting = true
        this.logLevel = level.pollWith()
        return this
    }

    /**
     * Whether to allow the initialized ZApi to use the Mock annotation. For the usage of Mock see @see [com.zj.api.mock.Mock]
     * */
    fun mockAble(b: Boolean): BaseApiProxy<T, ERROR_HANDLER> {
        this.mockAble = b
        return this
    }

    /**
     * Whether to allow log printing, it is allowed by default.
     * You can monitor real-time logs by setting [com.zj.api.utils.LoggerInterface].
     * It does not affect the monitoring of network activities and traffic changes.
     * */
    fun debugAble(b: Boolean): BaseApiProxy<T, ERROR_HANDLER> {
        this.debugAble = b
        LogUtils.debugAble = debugAble
        return this
    }

    /**
     * @param factory see [ApiFactory]
     * */
    fun build(factory: ApiFactory<T>? = null): T {
        if (!logLevelSetting && logLevel <= 0) logLevel = LogLevel.Internal.pollWith()
        val apiFactory = BaseApiFactory(clazz.simpleName, timeOut, header, baseUrl, certificate, factory ?: ApiFactory.Default(), debugAble, mockAble, logLevel, handler)
        return apiFactory.createService(clazz)
    }
}
