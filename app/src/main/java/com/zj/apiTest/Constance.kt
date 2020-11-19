package com.zj.apiTest

import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
import java.lang.NullPointerException
import java.util.*


object Constance {

    val cpvUrl = object : UrlProvider() {
        override fun url(): String {
            return "https://cpv.ccdev.lerjin.com/"
        }
    }

    val cpvHeader = object : HeaderProvider {
        override fun headers(): Map<String, String> {
            return hashMapOf<String, String>().apply {
                this["token"] = "OTY3MzE1M2ItZWY4Ny00MmI5LWFlY2YtNDJlNjFkZGIzMmI4"
                this["userId"] = "115559"
                this["ostype"] = "android"
                this["uuid"] = UUID.randomUUID().toString()
            }
        }
    }


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