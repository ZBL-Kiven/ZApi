package com.zj.apiTest

import com.zj.api.ZApi
import com.zj.api.call.coroutine.SuspendObservable
import com.zj.api.eh.ApiHandler
import com.zj.api.eh.EHParams
import com.zj.api.mock.Mock
import com.zj.apiTest.mock.MockTest
import com.zj.ok3.http.*
import io.reactivex.Observable

interface TestService {

    @Mock(MockTest::class)
    @GET("json/")
    fun getIpMock(@Query("lang") lang: String, @EHParams("aaa") p: String): Observable<Any>

    @GET("json/")
    fun getIp(@Query("lang") lang: String): Observable<Any>

    @ApiHandler(timeOut = 1000, successEHScope = ZApi.MAIN, errorEHScope = ZApi.IO, id = "first_test")
    @GET("json/")
    suspend fun getIpCourSimple(@Query("lang") lang: String): Any?

    @Mock(MockTest::class)
    @GET("json/")
    suspend fun getIpCour(@Query("lang") lang: String, @EHParams("bbb") s: String, @EHParams("aaa") p: String): SuspendObservable<Any>?
}