package com.zj.apiTest

import android.util.Log
import com.alibaba.fastjson.JSON
import com.zj.api.eh.EHParam
import com.zj.api.eh.ErrorHandler
import com.zj.api.exception.ApiException

object ApiErrorHandler : ErrorHandler() {

    class CusError {
        var code: Int = -1
        var lang: String? = ""
        var message: String? = ""
    }

    override suspend fun <R> interruptSuccessBody(id: String, code: Int, data: R?, ehParams: EHParam): R? {
        Log.e("-------- ", "$ehParams")
        return super.interruptSuccessBody(id, code, data, ehParams)
    }

    override suspend fun interruptErrorBody(throwable: ApiException?, ehParams: EHParam): Pair<Boolean, Any?> {
        val s = kotlin.runCatching {
            throwable?.httpException?.response()?.errorBody()?.string()
        }.getOrNull()
        val e = kotlin.runCatching { JSON.parseObject(s, CusError::class.java) }.getOrNull()
        return (Pair(false, e))
    }
}