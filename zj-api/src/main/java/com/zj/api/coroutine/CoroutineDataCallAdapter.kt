package com.zj.api.coroutine

import com.zj.api.interfaces.ErrorHandler
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class CoroutineDataCallAdapter<F>(private val returnType: Type, private val errorHandler: ErrorHandler?, private val preError: Throwable?, private val mockData: F?) : CallAdapter<F?, Call<F?>> {

    override fun responseType(): Type {
        return returnType
    }

    override fun adapt(call: Call<F?>): Call<F?> {
        return CoroutineDataCall(call, errorHandler, preError, mockData)
    }
}