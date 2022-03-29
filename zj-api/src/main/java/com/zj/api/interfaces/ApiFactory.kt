package com.zj.api.interfaces

import com.zj.api.base.BaseCallAdapterFactory
import com.zj.api.base.BaseHttpClientBuilder
import retrofit2.Converter
import retrofit2.Retrofit

abstract class ApiFactory<T> {

    /**
     * build your custom okHttpClient.
     * */
    open var okHttpClient: BaseHttpClientBuilder? = null

    /**
     * build your custom json converter.
     * */
    open var jsonConverter: Converter.Factory? = null

    /**
     * build your custom CallAdapterFactory. but it not retrofit factory, you'd try to  build with based BaseCallAdapterFactory.
     * */
    open var callAdapterFactory: BaseCallAdapterFactory? = null

    /**
     * if you're not use it with dynamic proxy , you'd better ignore this extend
     * */
    open fun createService(mRetrofit: Retrofit, cls: Class<T>): T {
        return mRetrofit.create(cls)
    }

    internal class Default<T> : ApiFactory<T>()
}
