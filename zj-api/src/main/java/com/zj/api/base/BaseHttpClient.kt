package com.zj.api.base

import com.zj.api.interceptor.HttpLoggingInterceptor
import com.zj.api.interceptor.Interceptor
import com.zj.api.interceptor.UrlProvider
import com.zj.api.utils.TrustAllCerts
import com.zj.api.utils.TrustAllHostnameVerifier
import com.zj.api.utils.getSslSocketFactory
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseHttpClient {

    open fun getHttpClient(clsName: String, header: MutableMap<String, String>? = null, url: UrlProvider?, logAble: Boolean, timeout: Long, certificate: Array<InputStream>? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(Interceptor(header, url))
        builder.connectTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.readTimeout(timeout * 2, TimeUnit.MILLISECONDS)
        builder.writeTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.buildSSLSocketFactory(certificate)
        val sslTrustManager = TrustAllHostnameVerifier()
        builder.hostnameVerifier(sslTrustManager)
        if (logAble) {
            builder.addInterceptor(HttpLoggingInterceptor(clsName).setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.protocols(Collections.unmodifiableList(listOf(Protocol.HTTP_1_1, Protocol.HTTP_2)))
        return builder.build()
    }

    private fun OkHttpClient.Builder.buildSSLSocketFactory(certificate: Array<InputStream>? = null): OkHttpClient.Builder {
        if (!certificate.isNullOrEmpty()) {
            val sslParams = getSslSocketFactory(certificate, null, null)
            val sslSocketFactory = sslParams.sSLSocketFactory
            val trustManager = sslParams.trustManager
            if (sslSocketFactory != null && trustManager != null) return this.sslSocketFactory(sslSocketFactory, trustManager)
        }
        val sslFactory = TrustAllCerts.createSSLSocketFactory() ?: return this
        return this.sslSocketFactory(sslFactory, TrustAllCerts)
    }
}