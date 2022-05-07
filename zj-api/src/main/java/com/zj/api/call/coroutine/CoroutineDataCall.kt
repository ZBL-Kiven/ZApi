package com.zj.api.call.coroutine

import com.zj.api.adapt.AdapterPendingData
import com.zj.api.utils.Constance
import com.zj.api.utils.LogUtils
import okhttp3.Request
import okio.Timeout
import com.zj.ok3.Call
import com.zj.ok3.Callback
import com.zj.ok3.HttpException
import com.zj.ok3.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class CoroutineDataCall<F : Any?>(private val region: Call<F?>, private val pendingData: AdapterPendingData<F>) : Call<F?> {

    override fun clone(): Call<F?> {
        return CoroutineDataCall(region, pendingData)
    }

    override fun execute(): Response<F?> {
        throw IllegalArgumentException("custom call should`nt have execute call!")
    }

    override fun enqueue(callback: Callback<F?>) {

        if (pendingData.mockData != null) {
            region.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                Constance.dealSuccessDataWithEh(pendingData, 200, pendingData.mockData.getMockData(pendingData.methodParamData)) {
                    onSuccess(callback, 200, it)
                }
            }
            return
        }
        if (pendingData.preError != null) {
            onError(400, pendingData.preError)
            return
        }
        val cb = object : Callback<F?> {

            override fun onResponse(call: Call<F?>, response: Response<F?>) {
                val body = response.body()
                val code = response.code()
                if (response.isSuccessful && body != null) {
                    try {
                        onSuccess(callback, code, body)
                    } catch (e: Exception) {
                        onError(code, e)
                    }
                } else {
                    onError(code, HttpException(response))
                }
            }

            override fun onFailure(call: Call<F?>, t: Throwable) {
                onError(400, t)
            }
        }
        region.enqueue(cb)
    }

    fun onError(code: Int, e: Throwable) {
        Constance.dealErrorWithEH(pendingData, code, this@CoroutineDataCall, e) { error, s ->
            LogUtils.e(pendingData.targetCls?.simpleName ?: "CoroutineDataCall", "CoroutineDataCall => Api(${region.request().url()}) using direct data request cannot return a error:\n\t${error.message}\n\tHandled = ${s.toString()}\n\tUse @SuspendObservable<Foo> to get error messages when requested by a coroutine.")
        }
    }

    private fun onSuccess(callback: Callback<F?>, code: Int, data: F?) {
        Constance.dealSuccessDataWithEh(pendingData, code, data) {
            val rsp: Response<F?> = Response.success(it)
            callback.onResponse(this@CoroutineDataCall, rsp)
        }
    }

    override fun isExecuted(): Boolean {
        return region.isExecuted
    }

    override fun cancel() {
        region.cancel()
    }

    override fun isCanceled(): Boolean {
        return region.isCanceled
    }

    override fun request(): Request {
        return region.request()
    }

    override fun timeout(): Timeout {
        return region.timeout()
    }
}