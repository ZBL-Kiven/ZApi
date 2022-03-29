package com.zj.api.base

import com.zj.api.adapt.ZApiCallAdapterFactory
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.api.eh.ErrorHandler
import com.zj.api.utils.Constance.parseOrCreateHttpException
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

internal class BaseRfFactory<T>(
    private val clsName: String,
    private val timeout: Long,
    private val header: MutableMap<String, String>?,
    private val urlProvider: UrlProvider?,
    private val certificate: Array<InputStream>?,
    private val factory: ApiFactory<T>,
    private val debugAble: Boolean,
    private val mockAble: Boolean,
    private val errorHandler: ErrorHandler?,
    private val preError: Throwable?,
) {

    private val getOkHttpClient: OkHttpClient by lazy {
        (factory.okHttpClient ?: BaseHttpClientBuilder()).getHttpClient(clsName, header, urlProvider, debugAble, timeout, certificate)
    }

    private val getJsonConverter: Converter.Factory by lazy {
        factory.jsonConverter ?: GsonConverterFactory.create()
    }

    private val getCallAdapterFactory: BaseCallAdapterFactory by lazy {
        factory.callAdapterFactory ?: ZApiCallAdapterFactory<T>(errorHandler, mockAble)
    }

    private val mRetrofit: Retrofit by lazy {
        initRetrofit()
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
        val service = factory.createService(mRetrofit, cls)
        val e = if (preError == null) null else parseOrCreateHttpException("before invoke", urlProvider?.url(), header, preError)
        getCallAdapterFactory.preError = e
        getCallAdapterFactory.targetCls = cls
        getCallAdapterFactory.timeOutDefault = timeout
        getCallAdapterFactory.timeOutChangeListener = onTimeoutChanged
        return service
    }

    private fun initRetrofit(): Retrofit {
        val retrofit = Retrofit.Builder()
        retrofit.baseUrl(urlProvider?.url() ?: "http://127.0.0.1")
        retrofit.client(getOkHttpClient)
        retrofit.addConverterFactory(getJsonConverter)
        retrofit.addCallAdapterFactory(getCallAdapterFactory)
        return retrofit.build()
    }

}
