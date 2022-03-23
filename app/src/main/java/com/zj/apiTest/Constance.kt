package com.zj.apiTest

import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.util.*


object Constance {

    val cpvUrl = object : UrlProvider() {
        override fun url(): String {
            return "https://api.ccdev.lerjin.com/"
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
                return "https://api.dev.utown.io:3080"

            //                return "http://ip-api.com/"
            }
        }
    }

    fun getHeader(): HeaderProvider {
        return object : HeaderProvider {
            override fun headers(): Map<String, String> {
                return mutableMapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzaWdudXBTdGF0dXMiOiJGQUNFX0RBVEEiLCJ1c2VySWQiOjEwMDAwNDEsInNleCI6Ik1BTEUiLCJuaWNrbmFtZSI6IkNlbCBcblxuXG5DZWwiLCJpYXQiOjE2NDgwMDI3NDQsImV4cCI6MTY1MDU5NDc0NH0.moudNFHTypvlwyW48Cuhj0monXZwg70XBsTgoj402J8",
                )
            }
        }
    }
}