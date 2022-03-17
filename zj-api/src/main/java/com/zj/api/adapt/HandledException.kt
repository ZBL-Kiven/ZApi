package com.zj.api.adapt

class HandledException(val raw: Throwable, val handledData: Any? = null) : Throwable()