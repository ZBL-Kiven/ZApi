package com.zj.api.interfaces

import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

abstract class ApiFactory<T> {

    open val getOkHttpClient: OkHttpClient? = null

    open val getJsonConverter: Converter.Factory? = null

    open val getCallAdapterFactory: CallAdapter.Factory? = null

    open val mRetrofit: Retrofit? = null

    open fun createService(mRetrofit: Retrofit, cls: Class<T>): T? {
        return mRetrofit.create(cls)
    }
}
