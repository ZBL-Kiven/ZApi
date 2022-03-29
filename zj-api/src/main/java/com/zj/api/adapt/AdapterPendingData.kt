package com.zj.api.adapt

import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.HandleScheduler

internal data class AdapterPendingData<T>(val targetCls: Class<*>?, val errorHandler: ErrorHandler?, val preError: Throwable?, val handleScheduler: HandleScheduler, val mockData: T?) {

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