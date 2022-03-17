package com.zj.api.utils

import android.util.Log
import com.zj.api.BuildConfig
import java.lang.Exception

@Suppress("unused")
internal object LogUtils {

    private val debugAble; get() = BuildConfig.DEBUG

    private const val TAG = " base.api ==> "

    fun d(s: String) {
        if (debugAble) Log.d(TAG, s)
    }

    fun e(s: String) {
        if (debugAble) Log.e(TAG, s)
    }

    fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) {
        try {
            lin[fromCls]?.onSizeParsed(fromCls, isSend, size)
            globalStreamInterface?.onSizeParsed(fromCls, isSend, size)
        } catch (e: Exception) {
            this.e(e.message ?: "onSizeParsed error!")
        }
    }

    private val lin = hashMapOf<String, LoggerInterface>()
    private var globalStreamInterface: LoggerInterface? = null

    fun setStreamingListener(cls: Class<*>, lin: LoggerInterface) {
        this.lin[cls.simpleName] = lin
    }

    fun setGlobalStreamingListener(global: LoggerInterface) {
        this.globalStreamInterface = global
    }
}

interface LoggerInterface {

    fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long)
}
