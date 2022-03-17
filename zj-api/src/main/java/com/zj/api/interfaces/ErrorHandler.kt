package com.zj.api.interfaces


interface ErrorHandler {

    fun interruptErrorBody(throwable: Throwable?): Pair<Boolean, Any?>

    fun <R> interruptSuccessBody(data: R?): R? {
        return data
    }
}
