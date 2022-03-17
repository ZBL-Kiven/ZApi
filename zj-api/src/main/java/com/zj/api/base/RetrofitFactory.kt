package com.zj.api.base

import com.zj.api.adapt.ZApiCallAdapterFactory
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.api.interfaces.ErrorHandler
import com.zj.api.utils.Constance.parseOrCreateHttpException
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

internal class RetrofitFactory<T>(
    private val creatable: Throwable?,
    private val clsName: String,
    private val timeout: Long,
    private val header: MutableMap<String, String>?,
    private val urlProvider: UrlProvider?,
    private val certificate: Array<InputStream>?,
    private val factory: ApiFactory<T>?,
    private val debugAble: Boolean,
    private val errorHandler: ErrorHandler?,
    private val preError: Throwable?,
) {

    private val getOkHttpClient: OkHttpClient by lazy {
        (factory?.okHttpClient ?: BaseHttpClient()).getHttpClient(clsName, header, urlProvider, debugAble, timeout, certificate)
    }

    private val getJsonConverter: Converter.Factory by lazy {
        factory?.jsonConverter ?: GsonConverterFactory.create()
    }

    private val getCallAdapterFactory: BaseCallAdapterFactory by lazy {
        factory?.callAdapterFactory ?: ZApiCallAdapterFactory<T>(errorHandler)
    }

    private val mRetrofit: Retrofit by lazy {
        factory?.mRetrofit ?: initRetrofit()
    }

    fun createService(cls: Class<T>): T {
        val service = factory?.createService(mRetrofit, cls) ?: mRetrofit.create(cls)
        val e = if (creatable == null) null else parseOrCreateHttpException(urlProvider?.url(), header, preError)
        getCallAdapterFactory.preError = e
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
