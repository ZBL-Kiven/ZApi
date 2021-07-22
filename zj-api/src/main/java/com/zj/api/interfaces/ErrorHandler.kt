package com.zj.api.interfaces

interface ErrorHandler {

    fun onError(throwable: Throwable?): Boolean

    fun interruptSuccessBody(data: Any?): Boolean {
        return false
    }
}
