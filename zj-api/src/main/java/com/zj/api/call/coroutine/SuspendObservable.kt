package com.zj.api.call.coroutine

import com.zj.api.exception.ApiException

data class SuspendObservable<F>(val data: F?, val error: ApiException? = null, val fromErrorHandler: Any?)