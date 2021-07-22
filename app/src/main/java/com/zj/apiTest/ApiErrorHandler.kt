package com.zj.apiTest

import android.util.Log
import com.zj.api.interfaces.ErrorHandler
import retrofit2.HttpException
import java.lang.Exception
import java.net.UnknownHostException

object ApiErrorHandler : ErrorHandler {

    override fun onError(throwable: Throwable?): Boolean {
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
        return false
    }

    override fun interruptSuccessBody(data: Any?): Boolean {
        Log.e("----- ", "test interrupt result data   $data")
        return false
    }
}