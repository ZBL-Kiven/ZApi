package com.zj.apiTest

import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider


object Constance {

    fun getBaseUrl(): UrlProvider {
        return object : UrlProvider() {
            override fun url(): String {
                return "http://ip-api.com/"
            }
        }
    }

    fun getHeader(): HeaderProvider {
        return object : HeaderProvider {
            override fun headers(): Map<String, String> {
                return hashMapOf<String, String>().apply {
                    this["Content-Type"] = "application/json"
                }
            }
        }
    }
}