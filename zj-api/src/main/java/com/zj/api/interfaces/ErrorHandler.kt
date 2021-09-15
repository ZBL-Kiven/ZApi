package com.zj.api.interfaces


interface ErrorHandler {

    fun onError(throwable: Throwable?): Pair<Boolean, Any?>

    fun interruptSuccessBody(data: Any?): Pair<Boolean, Any?> {
        return Pair(false, null)
    }
}
