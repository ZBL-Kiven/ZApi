package com.zj.apiTest

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface TestService {


    @GET("json/")
    fun getWeather(@Query("lang") lang: String): Observable<String>
}