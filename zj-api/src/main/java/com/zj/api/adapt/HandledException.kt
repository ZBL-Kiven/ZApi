package com.zj.api.adapt

import java.lang.Exception

class HandledException(val raw: Throwable, val handledData: Any? = null) : Exception()