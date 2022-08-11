package com.zj.api.base

import com.zj.api.eh.EHParam
import com.zj.ok3.CallAdapter

abstract class BaseCallAdapterFactory : CallAdapter.Factory() {

    internal var timeOutChangeListener: ((Int, Int, Int) -> Unit?)? = null

    internal var preError: Throwable? = null

    internal var targetCls: Class<*>? = null

    internal var timeOutDefault: Int = 0

    internal var methodParamData = EHParam()

    internal fun resetParamData() {
        methodParamData = EHParam()
    }
}