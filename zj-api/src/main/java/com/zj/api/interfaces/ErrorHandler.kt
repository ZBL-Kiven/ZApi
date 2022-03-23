package com.zj.api.interfaces

import com.zj.api.exception.ApiException

interface ErrorHandler {

    fun interruptErrorBody(throwable: ApiException?): Pair<Boolean, Any?>

    fun <R> interruptSuccessBody(data: R?): R? {
        return data
    }
}
