@file:Suppress("unused")

package com.zj.api.base

import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.api.interfaces.ErrorHandler
import com.zj.api.utils.LogUtils
import java.io.InputStream

class BaseApiProxy<T : Any, ERROR_HANDLER : ErrorHandler>(private val clazz: Class<T>, private val handler: ERROR_HANDLER? = null) {

    private var timeOut: Long = 10000
    private var header: HeaderProvider? = null
    private var baseUrl: UrlProvider? = null
    private var debugAble: Boolean = true
    private var mockAble: Boolean = true
    private var certificate: Array<InputStream>? = null

    fun certificate(certificate: Array<InputStream>): BaseApiProxy<T, ERROR_HANDLER> {
        this.certificate = certificate
        return this
    }

    fun baseUrl(url: UrlProvider): BaseApiProxy<T, ERROR_HANDLER> {
        this.baseUrl = url
        return this
    }

    fun header(header: HeaderProvider): BaseApiProxy<T, ERROR_HANDLER> {
        this.header = header
        return this
    }

    fun timeOut(timeOut: Long): BaseApiProxy<T, ERROR_HANDLER> {
        this.timeOut = timeOut
        return this
    }

    fun mockAble(b: Boolean): BaseApiProxy<T, ERROR_HANDLER> {
        this.mockAble = b
        return this
    }

    fun debugAble(b: Boolean): BaseApiProxy<T, ERROR_HANDLER> {
        this.debugAble = b
        LogUtils.debugAble = debugAble
        return this
    }

    fun build(factory: ApiFactory<T>? = null): T {
        val retrofitFactory = createRetrofitFactory(factory)
        return retrofitFactory.createService(clazz)
    }

    private fun createRetrofitFactory(factory: ApiFactory<T>?): RetrofitFactory<T> {
        val map = mutableMapOf<String, String>()
        var throwable: Throwable? = null
        try {
            header?.headers()?.let { map.putAll(it) }
        } catch (e: Throwable) {
            throwable = e
        } catch (e: Exception) {
            throwable = e
        } catch (e: java.lang.Exception) {
            throwable = e
        }
        return RetrofitFactory(clazz.simpleName, timeOut, map, baseUrl, certificate, factory ?: ApiFactory.Default(), debugAble, mockAble, handler, throwable)
    }
}
