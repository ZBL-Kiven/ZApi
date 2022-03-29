package com.zj.api.base

import retrofit2.CallAdapter

abstract class BaseCallAdapterFactory : CallAdapter.Factory() {

    internal var timeOutChangeListener: ((Int) -> Unit?)? = null

    internal var preError: Throwable? = null

    internal var targetCls: Class<*>? = null

    internal var timeOutDefault: Long = 0


}