package com.zj.apiTest

import io.reactivex.Observable
import retrofit2.http.*

interface TestService {

    /**
     * 用户行为收集
     */
    @FormUrlEncoded
    @POST("/behavior/event")
    fun behaviorEvent(@Field("eventType") eventType: String, @Field("sourceId") sourceId: String, @Field("pid") pid: String, @Field("feedName") feedName: String = ""): Observable<String>

    @GET("json/")
    fun getWeather(@Query("lang") lang: String): Observable<String>
}