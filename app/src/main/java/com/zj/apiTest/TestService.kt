package com.zj.apiTest

import com.zj.api.call.coroutine.SuspendObservable
import com.zj.api.eh.ApiHandler
import com.zj.api.mock.Mock
import com.zj.apiTest.mock.MockTest
import io.reactivex.Observable
import retrofit2.http.*

interface TestService {

    @Mock(MockTest::class)
    @GET("json/")
    fun getIpMock(@Query("lang") lang: String): Observable<Any>

    @GET("json/")
    fun getIp(@Query("lang") lang: String): Observable<Any>

    @ApiHandler(timeOut = 1000)
    @GET("json/")
    suspend fun getIpCourSimple(@Query("lang") lang: String): Any?

    @GET("json/")
    suspend fun getIpCour(@Query("lang") lang: String): SuspendObservable<Any>?
}