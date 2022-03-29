package com.zj.api.utils

import android.util.Log

@Suppress("unused")
internal object LogUtils {

    var debugAble = true

    private const val TAG = "ZApi.%s --> "

    fun d(target: String, s: String) {
        if (debugAble) Log.d(String.format(TAG, target), s)
        onLog(target, Log.DEBUG, s)
    }

    fun e(target: String, s: String) {
        if (debugAble) Log.e(String.format(TAG, target), s)
        onLog(target, Log.ERROR, s)
    }

    private fun onLog(target: String, level: Int, s: String) {
        try {
            lin[target]?.onLog(level, String.format(TAG, target), s)
            globalStreamInterface?.onLog(level, String.format(TAG, target), s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) {
        try {
            lin[fromCls]?.onSizeParsed(fromCls, isSend, size)
            globalStreamInterface?.onSizeParsed(fromCls, isSend, size)
        } catch (e: Exception) {
            this.e(fromCls, e.message ?: "onSizeParsed error!")
        }
    }

    private val lin = hashMapOf<String, LoggerInterface>()
    private var globalStreamInterface: LoggerInterface? = null

    /**
     * Add a traffic monitor to a Service
     * */
    fun addStreamingListener(cls: Class<*>, lin: LoggerInterface) {
        this.lin[cls.simpleName] = lin
    }

    /**
     * Set up traffic monitors for all services
     * */
    fun setGlobalStreamingListener(global: LoggerInterface) {
        this.globalStreamInterface = global
    }
}

interface LoggerInterface {

    fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long)

    fun onLog(level: Int, tag: String, s: String) {}
}
