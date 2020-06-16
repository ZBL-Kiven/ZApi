package com.zj.apiTest.config

import com.zj.api.BaseApi
import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.ApiFactory
import com.zj.apiTest.ApiErrorHandler
import com.zj.apiTest.Constance
import com.zj.apiTest.converter.FastJsonConverterFactory
import retrofit2.Converter

object TestApi {

    /**
     * 每个模块都可以单独使用配置，
     *
     * 配置包括 ： 「baseUrl ，header ，timeOut , certificate , errorHandler 」自定义
     *
     * 高级配置包括：interceptor , HttpClient ，DataConverter ， CallbackScheduler
     *
     * 高级扩展: Retrofit 实例自定义
     *
     * */

    inline fun <reified T : Any> getDefaultApi(baseUrl: UrlProvider = Constance.getBaseUrl(), header: HeaderProvider = Constance.getHeader(), timeOut: Long = 5000): BaseApi<T> {
        return BaseApi.create<T>(ApiErrorHandler).baseUrl(baseUrl).header(header).timeOut(timeOut).build(object : ApiFactory<T>() {

            override val getJsonConverter: Converter.Factory?
                get() = FastJsonConverterFactory.create()
        })
    }
}