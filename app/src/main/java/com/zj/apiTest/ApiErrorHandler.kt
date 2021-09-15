package com.zj.apiTest

import android.util.Log
import com.zj.api.interfaces.ErrorHandler
import retrofit2.HttpException
import java.lang.Exception
import java.net.UnknownHostException

object ApiErrorHandler : ErrorHandler {

    override fun onError(throwable: Throwable?): Pair<Boolean, Throwable?> {
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
        e.msg = "delegate"
        return Pair(false, e)
    }

    override fun interruptSuccessBody(data: Any?): Pair<Boolean, Throwable?> {
        Log.e("----- ", "test interrupt result data   $data")
        return Pair(false, null)
    }


    class CusError : Throwable() {
        var code: Int = -1
        var msg: String? = ""
    }
}