package com.zj.api.interfaces

import com.zj.api.adapt.EHParameterProxy
import com.zj.api.base.BaseCallAdapterFactory
import com.zj.api.base.BaseHttpClientBuilder
import com.zj.ok3.Converter
import com.zj.ok3.ZHttpServiceCreator

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
     * build your custom CallAdapterFactory. but it not hsc factory, you'd try to  build with based BaseCallAdapterFactory.
     * */
    open var callAdapterFactory: BaseCallAdapterFactory? = null

    /**
     * if you're not use it with dynamic proxy , you'd better ignore this extend
     * */
    open fun createService(mZHttpServiceCreator: ZHttpServiceCreator, cls: Class<T>, lazyParamFinder: (Boolean, MutableMap<String, Any?>) -> Unit): T {
        return EHParameterProxy.create(mZHttpServiceCreator, cls, lazyParamFinder)
    }

    internal class Default<T> : ApiFactory<T>()
}
