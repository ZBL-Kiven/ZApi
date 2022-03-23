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
                    "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2NDgwMzAzOTgsImV4cCI6MTY1MDYyMjM5OCwiand0X3VzZXIiOnsiZ3VpZCI6Ijc4NjMzMTE4YjMyODQ5MTk5OTkyZGQ0MGZiOGE2OTc0IiwidXNlcklkIjoxMDAwMDQxLCJpZGVudGlmaWVyIjoiempqMDg4OEBnbWFpbC5jb20iLCJuaWNrbmFtZSI6IkNlbCBcblxuXG5DZWwiLCJhdmF0YXIiOm51bGwsImZhY2UiOiJodHRwczovL2Nkbi5kZXYudXRvd24uaW8vaS8yMDIyMDMyMy8zLzIvOC8zMjgyYmYyODhjOGU0YzBjOWFjZDMwZWNkMjZlNDk2Yy40OTkyMzY2Nzk5NCIsImFub255bW91cyI6ZmFsc2V9fQ.ZSsleMgjPY1Si7NNi_G55pDOHcA8k1_fZuRzt-Mu59w",
                )
            }
        }
    }
}