package com.zj.api.base

import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.api.retrofit.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

class RetrofitFactory<T> internal constructor(internal val valuable: Boolean, private val timeout: Long, internal val header: MutableMap<String, String>? = null, internal val urlProvider: UrlProvider?, private val certificate: Array<InputStream>? = null, private val factory: ApiFactory<T>? = null) {

    private val debugAble: Boolean; get() = factory?.debugAble ?: true

    private val getOkHttpClient: OkHttpClient; get() = factory?.getOkHttpClient ?: BaseHttpClient(header, urlProvider, debugAble).getHttpClient(timeout, certificate)

    private val getJsonConverter: Converter.Factory; get() = factory?.getJsonConverter ?: GsonConverterFactory.create()

    private val getCallAdapterFactory: CallAdapter.Factory; get() = factory?.getCallAdapterFactory ?: RxJava2CallAdapterFactory.createAsync()

    private val mRetrofit: Retrofit; get() = factory?.mRetrofit ?: initRetrofit()

    internal fun createService(cls: Class<T>): T {
        return factory?.createService(mRetrofit, cls) ?: mRetrofit.create(cls)
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
