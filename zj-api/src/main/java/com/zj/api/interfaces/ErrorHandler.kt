package com.zj.api.interfaces

import com.zj.api.exception.ApiException

interface ErrorHandler {

    fun interruptErrorBody(throwable: ApiException?): Pair<Boolean, Any?>

    fun <R> interruptSuccessBody(code: Int, data: R?): R? {
        return data
    }
}
