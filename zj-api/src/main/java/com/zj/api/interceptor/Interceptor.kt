package com.zj.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class Interceptor(private val headerProvider: HeaderProvider? = null, private val urlProvider: UrlProvider?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response? {
        val request = chain.request()
        val newBuilder = request.newBuilder()
        headerProvider?.headers()?.forEach {
            newBuilder.addHeader(it.key, it.value)
        }
        urlProvider?.url()?.let {
            val proxy: UrlProvider.UrlProxy = urlProvider.getProxy()
            newBuilder.url(request.url().newBuilder().scheme(proxy.protocol).host(proxy.host).port(proxy.port).build())
        }
        return try {
            chain.proceed(newBuilder.build())
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            chain.proceed(request)
        } catch (e: IOException) {
            e.printStackTrace()
            chain.proceed(request)
        }
    }
}