package com.zj.apiTest

import com.zj.api.exception.ApiException
import com.zj.api.interfaces.ErrorHandler

object ApiErrorHandler : ErrorHandler {

    class CusError {
        var code: Int = -1
        var msg: String? = ""
    }

    override fun interruptErrorBody(throwable: ApiException?): Pair<Boolean, Any?> {
        val e = CusError()
        e.code = 1000
        e.msg = "test delegate error body!"
        return Pair(false, e)
    }
}