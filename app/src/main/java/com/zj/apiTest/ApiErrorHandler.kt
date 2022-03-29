package com.zj.apiTest

import com.alibaba.fastjson.JSON
import com.zj.api.exception.ApiException
import com.zj.api.eh.ErrorHandler

object ApiErrorHandler : ErrorHandler() {

    class CusError {
        var code: Int = -1
        var lang: String? = ""
        var message: String? = ""
    }

    override suspend fun interruptErrorBody(throwable: ApiException?): Pair<Boolean, Any?> {
        val s = kotlin.runCatching {
            throwable?.httpException?.response()?.errorBody()?.string()
        }.getOrNull()
        val e = kotlin.runCatching { JSON.parseObject(s, CusError::class.java) }.getOrNull()
        return (Pair(false, e))
    }
}