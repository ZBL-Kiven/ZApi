package com.zj.api.adapt

import androidx.lifecycle.LifecycleOwner
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.HandleScheduler
import com.zj.api.utils.Constance

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

    fun <T> success(lo: LifecycleOwner, l: suspend ErrorHandler.() -> T?) {
        errorHandler?.let {
            Constance.withScheduler(it, handleScheduler.successScope, lo, l)
        }
    }

    fun <T> error(lo: LifecycleOwner, l: suspend ErrorHandler.() -> T?) {
        errorHandler?.let {
            Constance.withScheduler(it, handleScheduler.errorScope, lo, l)
        }
    }
}