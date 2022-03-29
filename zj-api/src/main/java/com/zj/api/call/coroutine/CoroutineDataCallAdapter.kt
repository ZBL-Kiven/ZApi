package com.zj.api.call.coroutine

import com.zj.api.adapt.AdapterPendingData
import com.zj.api.eh.ErrorHandler
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class CoroutineDataCallAdapter<F>(private val returnType: Type, private val pendingData: AdapterPendingData<F?>) : CallAdapter<F?, Call<F?>> {

    override fun responseType(): Type {
        return returnType
    }

    override fun adapt(call: Call<F?>): Call<F?> {
        return CoroutineDataCall(call, pendingData)
    }
}