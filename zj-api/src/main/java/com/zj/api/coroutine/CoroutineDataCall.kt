package com.zj.api.coroutine

import com.zj.api.interfaces.ErrorHandler
import com.zj.api.utils.Constance
import com.zj.api.utils.LogUtils
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

internal class CoroutineDataCall<F : Any?>(private val region: Call<F?>, private val errorHandler: ErrorHandler?, private val preError: Throwable?) : Call<F?> {

    override fun clone(): Call<F?> {
        return CoroutineDataCall(region, errorHandler, preError)
    }

    override fun execute(): Response<F?> {
        throw IllegalArgumentException("custom call should`nt have execute call!")
    }

    override fun enqueue(callback: Callback<F?>) {

        fun onError(code: Int, e: Throwable) {
            Constance.dealErrorWithEH(errorHandler, code, this@CoroutineDataCall, e) { error, s ->
                LogUtils.e("CoroutineDataCall => Api(${region.request().url()}) using direct data request cannot return a error:\n\t${error.message()}\n\tHandled = ${s.toString()}\n\tUse @SuspendObservable<Foo> to get error messages when requested by a coroutine.")
            }
        }

        if (preError != null) {
            onError(400, preError)
            return
        }
        val cb = object : Callback<F?> {

            override fun onResponse(call: Call<F?>, response: Response<F?>) {
                val body = response.body()
                val code = response.code()
                if (response.isSuccessful && body != null) {
                    try {
                        Constance.dealSuccessDataWithEh(errorHandler, body) {
                            val rsp = Response.success(response.code(), body)
                            callback.onResponse(this@CoroutineDataCall, rsp)
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