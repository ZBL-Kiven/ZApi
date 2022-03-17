package com.zj.apiTest

import com.zj.api.coroutine.SuspendObservable
import com.zj.api.mock.Mock
import com.zj.apiTest.mock.MockTest
import io.reactivex.Observable
import retrofit2.http.*

interface TestService {

    /**
     * 用户行为收集
     */
    @FormUrlEncoded
    @POST("/behavior/event")
    fun behaviorEvent(@Field("eventType") eventType: String, @Field("sourceId") sourceId: String, @Field("pid") pid: String, @Field("feedName") feedName: String = ""): Observable<String>

    @POST("/payerMaxApi/getOtherPayInfo")
    fun getOtherPayInfo(@Body lang: String): Observable<String?>

    @GET("json/")
    fun getIp(@Query("lang") lang: String): Observable<Any>

    @GET("json/")
    suspend fun getIpCourSimple(@Query("lang") lang: String): String?

    @GET("json/")
    suspend fun getIpCour(@Query("lang") lang: String): SuspendObservable<Any>?
}