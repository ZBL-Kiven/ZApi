package com.zj.apiTest

import io.reactivex.Observable
import retrofit2.http.GET

interface TestService {


    @GET("json/")
    fun getWeather(lang:String): Observable<String>
}