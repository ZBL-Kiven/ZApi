package com.zj.api.interceptor

import com.zj.api.utils.Constance.HTTPS
import java.net.URL

abstract class UrlProvider {

    abstract fun url(): String

    internal fun getProxy(): UrlProxy {
        return UrlProxy(url())
    }

    internal class UrlProxy(u: String) {

        val protocol: String
        val port: Int
        val host: String

        init {
            val url = URL(u)
            protocol = url.protocol
            val isHttps = protocol.contains(HTTPS, ignoreCase = true)
            port = when (url.port) {
                -1 -> if (isHttps) 443 else 80
                else -> url.port
            }
            host = url.host
        }
    }
}