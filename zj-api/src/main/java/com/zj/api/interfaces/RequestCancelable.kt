package com.zj.api.interfaces

/**
 * cancel invoke or executor task
 * */
interface RequestCancelable {
    fun cancel(msg: String? = null, throwable: Throwable? = null)
}