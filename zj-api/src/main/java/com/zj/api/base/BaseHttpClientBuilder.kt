package com.zj.api.base

import com.zj.api.interceptor.*
import com.zj.api.utils.TrustAllCerts
import com.zj.api.utils.TrustAllHostnameVerifier
import com.zj.api.utils.getSslSocketFactory
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseHttpClientBuilder {

    open fun getHttpClient(clsName: String, header: HeaderProvider?, url: UrlProvider?, logAble: Boolean, timeout: Long, logLevel: Int, certificate: Array<InputStream>? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(Interceptor(header, url))
        builder.callTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.connectTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.readTimeout(timeout * 2, TimeUnit.MILLISECONDS)
        builder.writeTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.buildSSLSocketFactory(certificate)
        val sslTrustManager = TrustAllHostnameVerifier()
        builder.hostnameVerifier(sslTrustManager)
        if (logAble) {
            builder.addInterceptor(HttpLoggingInterceptor(clsName).setLevel(logLevel))
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