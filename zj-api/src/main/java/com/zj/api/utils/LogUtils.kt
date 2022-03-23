package com.zj.api.utils

import android.util.Log

@Suppress("unused")
internal object LogUtils {

    var debugAble = true

    private const val TAG = "zj.api ==> "

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
