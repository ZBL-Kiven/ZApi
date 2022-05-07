package com.zj.api.adapt

import com.zj.api.eh.EHParam
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.HandleScheduler
import com.zj.api.mock.MockAble

internal data class AdapterPendingData<T>(val targetCls: Class<*>?, val errorHandler: ErrorHandler?, val preError: Throwable?, val handleScheduler: HandleScheduler, val mockData: MockAble<T>?, val methodParamData: EHParam) {

    val isTimeOut: Boolean
        get() {
            return handleThreadTime <= 0
        }

    private var handleThreadTime: Long = handleScheduler.timeOut

    fun perWait(mils: Long) {
        handleThreadTime -= mils
    }

    fun resetTimeOut() {
        handleThreadTime = handleScheduler.timeOut
    }
}