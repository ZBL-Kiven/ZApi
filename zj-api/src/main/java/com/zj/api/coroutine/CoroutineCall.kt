package com.zj.api.coroutine

import com.zj.api.interfaces.ErrorHandler
import com.zj.api.utils.Constance
import okhttp3.*
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.lang.IllegalArgumentException

internal class CoroutineCall<F>(private val region: Call<F?>, private val errorHandler: ErrorHandler?, private val preError: Throwable?) : Call<SuspendObservable<F?>?> {

    override fun clone(): Call<SuspendObservable<F?>?> {
        return CoroutineCall(region, errorHandler, preError)
    }

    override fun execute(): Response<SuspendObservable<F?>?> {
        throw IllegalArgumentException("custom call should`nt have execute call!")
    }

    override fun enqueue(callback: Callback<SuspendObservable<F?>?>) {
        if (preError != null) {
            Constance.dealErrorWithEH(errorHandler, 400, region, preError) { error, s ->
                val resp = SuspendObservable<F?>(null, error, s)
                callback.onResponse(this, Response.success(resp))
            }
            return
        }
        val cb = object : Callback<F?> {

            override fun onResponse(call: Call<F?>, response: Response<F?>) {
                val body = response.body()
                val code = response.code()
                if (response.isSuccessful && body != null) {
                    try {
                        Constance.dealSuccessDataWithEh(errorHandler, body) {
                            val rsp: Response<SuspendObservable<F?>?> = Response.success(code, SuspendObservable(it, null, null))
                            callback.onResponse(this@CoroutineCall, rsp)
                        }
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

            private fun onError(code: Int, e: Throwable) {
                Constance.dealErrorWithEH(errorHandler, code, this@CoroutineCall, e) { error, s ->
                    val resp = SuspendObservable<F?>(null, error, s)
                    callback.onResponse(this@CoroutineCall, Response.success(resp))
                }
            }
        }
        region.enqueue(cb)
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