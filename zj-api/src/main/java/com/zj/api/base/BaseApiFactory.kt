package com.zj.api.base

import com.zj.api.adapt.ZApiCallAdapterFactory
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.api.eh.ErrorHandler
import com.zj.api.interceptor.LogLevel
import com.zj.api.utils.Constance.parseOrCreateHttpException
import com.zj.ok3.Converter
import com.zj.ok3.ZHttpServiceCreator
import com.zj.ok3.converter.GsonConverterFactory
import java.io.InputStream
import okhttp3.OkHttpClient

internal class BaseApiFactory<T>(
    private val clsName: String,
    private val timeout: Long,
    private val header: MutableMap<String, String?>?,
    private val urlProvider: UrlProvider?,
    private val certificate: Array<InputStream>?,
    private val factory: ApiFactory<T>,
    private val debugAble: Boolean,
    private val mockAble: Boolean,
    private val logLevel: LogLevel,
    private val errorHandler: ErrorHandler?,
    private val preError: Throwable?,
) {

    private val getOkHttpClient: OkHttpClient by lazy {
        (factory.okHttpClient ?: BaseHttpClientBuilder()).getHttpClient(clsName, header, urlProvider, debugAble, timeout, logLevel, certificate)
    }

    private val getJsonConverter: Converter.Factory by lazy {
        factory.jsonConverter ?: GsonConverterFactory.create()
    }

    private val getCallAdapterFactory: BaseCallAdapterFactory by lazy {
        factory.callAdapterFactory ?: ZApiCallAdapterFactory<T>(errorHandler, mockAble)
    }

    private val mZHttpServiceCreator: ZHttpServiceCreator by lazy {
        initCreator()
    }

    private val onTimeoutChanged = { l: Int ->
        val callTimeout = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("callTimeout") }.getOrNull()
        val connectTimeoutMillis = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("connectTimeout") }.getOrNull()
        val readTimeoutMillis = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("readTimeout") }.getOrNull()
        val writeTimeoutMillis = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("writeTimeout") }.getOrNull()
        callTimeout?.isAccessible = true
        connectTimeoutMillis?.isAccessible = true
        readTimeoutMillis?.isAccessible = true
        writeTimeoutMillis?.isAccessible = true
        callTimeout?.set(getOkHttpClient, l)
        connectTimeoutMillis?.set(getOkHttpClient, l)
        readTimeoutMillis?.set(getOkHttpClient, l)
        writeTimeoutMillis?.set(getOkHttpClient, l)
    }

    fun createService(cls: Class<T>): T {
        val service = factory.createService(mZHttpServiceCreator, cls) {
            it.forEach { (k, v) ->
                if (v != null) getCallAdapterFactory.methodParamData.addData(k, v)
            }
        }
        getCallAdapterFactory.resetParamData()
        val e = if (preError == null) null else parseOrCreateHttpException("before invoke", urlProvider?.url(), header, preError)
        getCallAdapterFactory.preError = e
        getCallAdapterFactory.targetCls = cls
        getCallAdapterFactory.timeOutDefault = timeout
        getCallAdapterFactory.timeOutChangeListener = onTimeoutChanged
        return service
    }

    private fun initCreator(): ZHttpServiceCreator {
        val hsc = ZHttpServiceCreator.Builder()
        hsc.baseUrl(urlProvider?.url() ?: "http://127.0.0.1")
        hsc.client(getOkHttpClient)
        hsc.addConverterFactory(getJsonConverter)
        hsc.addCallAdapterFactory(getCallAdapterFactory)
        return hsc.build()
    }
}
