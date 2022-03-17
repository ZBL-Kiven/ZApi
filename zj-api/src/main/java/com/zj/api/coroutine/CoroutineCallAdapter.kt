package com.zj.api.coroutine

import com.zj.api.interfaces.ErrorHandler
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class CoroutineCallAdapter<F>(private val returnType: Type, private val errorHandler: ErrorHandler?, private val preError: Throwable?) : CallAdapter<F?, Call<SuspendObservable<F?>?>> {

    override fun responseType(): Type {
        return returnType
    }

    override fun adapt(call: Call<F?>): Call<SuspendObservable<F?>?> {
        return CoroutineCall(call, errorHandler, preError)
    }
}