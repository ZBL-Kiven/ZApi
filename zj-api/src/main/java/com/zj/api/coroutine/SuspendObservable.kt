package com.zj.api.coroutine

import retrofit2.HttpException

data class SuspendObservable<F>(val data: F?, val error: HttpException? = null, val fromErrorHandler: Any?)