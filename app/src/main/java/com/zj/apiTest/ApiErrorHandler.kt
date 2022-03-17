package com.zj.apiTest

import android.util.Log
import com.zj.api.interfaces.ErrorHandler
import retrofit2.HttpException
import java.lang.Exception
import java.net.UnknownHostException

object ApiErrorHandler : ErrorHandler {

    class CusError {
        var code: Int = -1
        var msg: String? = ""
    }

    override fun <R> interruptSuccessBody(data: R?): R? {
        return super.interruptSuccessBody(data)
    }

    override fun interruptErrorBody(throwable: HttpException?): Pair<Boolean, Any?> {
        if (throwable is HttpException) {
            try {
                val errorInfo = throwable.response()?.body()?.toString()
                Log.e("http test", "onHttpError ----- case: $errorInfo")
            } catch (e: Exception) {
                Log.e("http test", "onHttpError ----- case: ${e.message}")
            }
        } else {
            if (throwable is UnknownHostException) {
                Log.e("http test", "net work error")
            } else {
                Log.e("http test", "onHttpError ----- case: ${throwable?.message}")
            }
        }
        throwable?.printStackTrace()
        val e = CusError()
        e.code = 1000
        e.msg = "test delegate error body!"
        return Pair(false, e)
    }
}