package com.zj.api.interfaces

interface ErrorHandler {
    fun onError(throwable: Throwable)
}
