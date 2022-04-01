package com.zj.apiTest

import android.util.Log
import com.alibaba.fastjson.JSON
import com.zj.api.exception.ApiException
import com.zj.api.eh.ErrorHandler

object ApiErrorHandler : ErrorHandler() {

    class CusError {
        var code: Int = -1
        var lang: String? = ""
        var message: String? = ""
    }

    override suspend fun <R> interruptSuccessBody(id: String, code: Int, data: R?): R? {
        Log.e("------- ", "transaction interruptSuccessBody")
        return super.interruptSuccessBody(id, code, data)
    }

    override suspend fun interruptErrorBody(throwable: ApiException?): Pair<Boolean, Any?> {
        Log.e("------- ", "transaction interruptErrorBody")
        val s = kotlin.runCatching {
            throwable?.httpException?.response()?.errorBody()?.string()
        }.getOrNull()
        val e = kotlin.runCatching { JSON.parseObject(s, CusError::class.java) }.getOrNull()
        return (Pair(false, e))
    }
}