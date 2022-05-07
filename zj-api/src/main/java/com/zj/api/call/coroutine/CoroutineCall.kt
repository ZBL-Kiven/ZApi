package com.zj.api.call.coroutine

import com.zj.api.adapt.AdapterPendingData
import com.zj.api.utils.Constance
import com.zj.ok3.*
import com.zj.ok3.Call
import com.zj.ok3.Callback
import com.zj.ok3.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okio.Timeout
import java.lang.IllegalArgumentException

internal class CoroutineCall<F>(private val region: Call<F?>, private val pendingData: AdapterPendingData<F>) : Call<SuspendObservable<F?>?> {

    override fun clone(): Call<SuspendObservable<F?>?> {
        return CoroutineCall(region, pendingData)
    }

    override fun execute(): Response<SuspendObservable<F?>?> {
        throw IllegalArgumentException("custom call should`nt have execute call!")
    }

    override fun enqueue(callback: Callback<SuspendObservable<F?>?>) {
        if (pendingData.mockData != null) {
            region.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                val mockData = pendingData.mockData.getMockData(pendingData.methodParamData)
                onSuccess(callback, 200, mockData)
            }
            return
        }
        if (pendingData.preError != null) {
            onError(callback, 400, pendingData.preError)
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
                        onError(callback, code, e)
                    }
                } else {
                    onError(callback, code, HttpException(response))
                }
            }

            override fun onFailure(call: Call<F?>, t: Throwable) {
                onError(callback, 400, t)
            }
        }
        region.enqueue(cb)
    }

    private fun onError(callback: Callback<SuspendObservable<F?>?>, code: Int, e: Throwable) {
        Constance.dealErrorWithEH(pendingData, code, region, e) { error, s ->
            val resp = SuspendObservable<F?>(null, error, s)
            callback.onResponse(this, Response.success(resp))
        }
    }

    private fun onSuccess(callback: Callback<SuspendObservable<F?>?>, code: Int, data: F?) {
        Constance.dealSuccessDataWithEh(pendingData, code, data) {
            val rsp: Response<SuspendObservable<F?>?> = Response.success(code, SuspendObservable(it, null, null))
            callback.onResponse(this@CoroutineCall, rsp)
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