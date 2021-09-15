package com.zj.api.interfaces

interface ErrorHandler {

    fun onError(throwable: Throwable?): Pair<Boolean, Throwable?>

    fun interruptSuccessBody(data: Any?): Pair<Boolean, Throwable?> {
        return Pair(false, null)
    }
}
