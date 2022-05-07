package com.zj.api.call.coroutine

import com.zj.api.adapt.AdapterPendingData
import com.zj.api.utils.Constance
import com.zj.ok3.Call
import com.zj.ok3.CallAdapter
import java.lang.reflect.Type

internal class CoroutineCallAdapter<F>(private val returnType: Type, private val pendingData: AdapterPendingData<F>) : CallAdapter<F?, Call<SuspendObservable<F?>?>> {

    override fun responseType(): Type {
        return returnType
    }

    override fun adapt(call: Call<F?>): Call<SuspendObservable<F?>?> {
        Constance.checkMockedValid(pendingData, returnType)
        return CoroutineCall(call, pendingData)
    }
}