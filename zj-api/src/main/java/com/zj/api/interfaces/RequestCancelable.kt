package com.zj.api.interfaces

interface RequestCancelable {
    fun cancel(msg: String? = null, throwable: Throwable? = null)
}