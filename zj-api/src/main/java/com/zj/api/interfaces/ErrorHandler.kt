package com.zj.api.interfaces

import retrofit2.HttpException


interface ErrorHandler {

    fun interruptErrorBody(throwable: HttpException?): Pair<Boolean, Any?>

    fun <R> interruptSuccessBody(data: R?): R? {
        return data
    }
}
