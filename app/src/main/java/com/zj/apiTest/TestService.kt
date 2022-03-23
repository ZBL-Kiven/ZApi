package com.zj.apiTest

import com.zj.api.coroutine.SuspendObservable
import com.zj.api.mock.Mock
import com.zj.apiTest.mock.MeetInfo
import com.zj.apiTest.mock.MockTest
import io.reactivex.Observable
import retrofit2.http.*

interface TestService {

    @POST("/payerMaxApi/getOtherPayInfo")
    fun getOtherPayInfo(@Body lang: String): Observable<String?>


    @Mock(MockTest::class)
    @GET("json/")
    fun getIp(@Query("lang") lang: String): Observable<Any>

    @GET("json/")
    suspend fun getIpCourSimple(@Query("lang") lang: String): String?

    @GET("json/")
    suspend fun getIpCour(@Query("lang") lang: String): SuspendObservable<Any>?


    @POST("/app/scene-support/meeting/create")
    fun createMeeting(@Body param: MeetInfo = MeetInfo()): Observable<Boolean>
}