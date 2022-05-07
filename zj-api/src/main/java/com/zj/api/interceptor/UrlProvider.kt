package com.zj.api.interceptor

import java.net.URL

abstract class UrlProvider {

    companion object {
        fun createStatic(s: String): UrlProvider {
            return object : UrlProvider() {
                override fun url(): String {
                    return s
                }
            }
        }

        fun create(url: () -> String): UrlProvider {
            return object : UrlProvider() {
                override fun url(): String {
                    return url.invoke()
                }
            }
        }
    }

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
            val isHttps = protocol.contains("https", ignoreCase = true)
            port = when (url.port) {
                -1 -> if (isHttps) 443 else 80
                else -> url.port
            }
            host = url.host
        }
    }
}

operator fun UrlProvider.plus(s: String): UrlProvider {
    return UrlProvider.create {
        this@plus.url() + s
    }
}