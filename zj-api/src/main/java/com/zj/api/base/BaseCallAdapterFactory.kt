package com.zj.api.base

import retrofit2.CallAdapter

abstract class BaseCallAdapterFactory : CallAdapter.Factory() {
    internal var preError: Throwable? = null
}