package com.zj.api.base

import com.zj.api.adapt.ZApiCallAdapterFactory
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.api.eh.ErrorHandler
import com.zj.api.interceptor.HeaderProvider
import com.zj.ok3.Converter
import com.zj.ok3.ZHttpServiceCreator
import com.zj.ok3.converter.GsonConverterFactory
import java.io.InputStream
import okhttp3.OkHttpClient

internal class BaseApiFactory<T>(
    private val clsName: String,
    private val timeout: Int,
    private val header: HeaderProvider?,
    private val urlProvider: UrlProvider?,
    private val certificate: Array<InputStream>?,
    private val factory: ApiFactory<T>,
    private val debugAble: Boolean,
    private val mockAble: Boolean,
    private val logLevel: Int,
    private val errorHandler: ErrorHandler?,
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

    private val onTimeoutChanged = { l: Int, r: Int, w: Int ->
        if (l > 0) {
            val connectTimeoutMillis = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("connectTimeout") }.getOrNull()
            connectTimeoutMillis?.isAccessible = true
            connectTimeoutMillis?.set(getOkHttpClient, l)
        }
        if (r > 0) {
            val readTimeoutMillis = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("readTimeout") }.getOrNull()
            readTimeoutMillis?.isAccessible = true
            readTimeoutMillis?.set(getOkHttpClient, r)
        }
        if (w > 0) {
            val writeTimeoutMillis = kotlin.runCatching { getOkHttpClient::class.java.getDeclaredField("writeTimeout") }.getOrNull()
            writeTimeoutMillis?.isAccessible = true
            writeTimeoutMillis?.set(getOkHttpClient, w)
        }
    }

    fun createService(cls: Class<T>): T {
        var throwable: Throwable? = null
        val service = try {
            factory.createService(mZHttpServiceCreator, cls) { clear, map ->
                if (clear) {
                    getCallAdapterFactory.resetParamData()
                } else map.forEach { (k, v) ->
                    if (v != null) getCallAdapterFactory.methodParamData.addData(k, v)
                }
            }
        } catch (e: Exception) {
            throwable = e
            mZHttpServiceCreator.create(cls, null)
        }
        getCallAdapterFactory.targetCls = cls
        getCallAdapterFactory.timeOutDefault = timeout
        getCallAdapterFactory.timeOutChangeListener = onTimeoutChanged
        getCallAdapterFactory.preError = throwable
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
