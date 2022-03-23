package com.zj.api.interfaces

import com.zj.api.exception.ApiException

interface ResponseHandler<T, R> {

    fun onSuccess(code: Int, content: T?, r: R)

    fun onError(e: ApiException, handled: Any?, r: R)

}