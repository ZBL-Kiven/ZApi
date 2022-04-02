package com.zj.api.utils

import android.util.Log
import com.zj.api.ZApi
import org.json.JSONObject

@Suppress("unused")
internal object LogUtils {

    var debugAble = true

    private const val TAG = "ZApi.%s "

    fun d(target: String, s: String) {
        if (debugAble) Log.d(String.format(TAG, target), s)
        Constance.withScheduler(LogUtils, ZApi.MAIN, null) {
            onLog(target, Log.DEBUG, s)
        }
    }

    fun e(target: String, s: String) {
        if (debugAble) Log.e(String.format(TAG, target), s)
        Constance.withScheduler(LogUtils, ZApi.MAIN, null) {
            onLog(target, Log.ERROR, s)
        }
    }

    private fun onLog(target: String, level: Int, s: String) {
        try {
            lin[target]?.onLog(level, String.format(TAG, target), s)
            globalStreamInterface?.onLog(level, String.format(TAG, target), s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long, vararg msg: Pair<String, String>) {
        try {
            val obj = JSONObject()
            msg.forEach {
                obj.put(it.first, it.second)
            }
            Constance.withScheduler(LogUtils, ZApi.MAIN, null) {
                notifyFlowChange(fromCls, isSend, size, obj.toString())
            }
        } catch (e: Exception) {
            this.e(fromCls, e.message ?: "onSizeParsed error!")
        }
    }

    private fun notifyFlowChange(fromCls: String, isSend: Boolean, size: Long, msg: String) {
        lin[fromCls]?.onSizeParsed(fromCls, isSend, size, msg)
        globalStreamInterface?.onSizeParsed(fromCls, isSend, size, msg)
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

    fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long, msg: String)

    fun onLog(level: Int, tag: String, s: String) {}
}
