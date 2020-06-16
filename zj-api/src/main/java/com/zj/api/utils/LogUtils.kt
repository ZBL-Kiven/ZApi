package com.zj.api.utils

import android.util.Log
import com.zj.api.BuildConfig

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

}
