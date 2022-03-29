package com.zj.api.eh

internal data class HandleScheduler(val successScope: String = ErrorHandler.MAIN, val errorScope: String, internal val id: String, val timeOut: Long)