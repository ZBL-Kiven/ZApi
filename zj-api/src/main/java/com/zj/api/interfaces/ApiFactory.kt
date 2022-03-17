package com.zj.api.interfaces

import com.zj.api.base.BaseCallAdapterFactory
import com.zj.api.base.BaseHttpClient
import retrofit2.Converter
import retrofit2.Retrofit

abstract class ApiFactory<T> {


    open var okHttpClient: BaseHttpClient? = null

    open var jsonConverter: Converter.Factory? = null

    open var callAdapterFactory: BaseCallAdapterFactory? = null

    open var mRetrofit: Retrofit? = null

    open fun createService(mRetrofit: Retrofit, cls: Class<T>): T {
        return mRetrofit.create(cls)
    }

    class Default<T> : ApiFactory<T>()
}
