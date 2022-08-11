package com.zj.api.eh

import com.zj.api.ZApi

internal data class HandleScheduler(val successScope: String = ZApi.MAIN, val errorScope: String, internal val id: String, val timeOut: Int)