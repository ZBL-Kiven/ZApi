package com.zj.ok3

import java.lang.reflect.Method

internal interface MethodHandler{

    fun parseParameterMaps(method: Method, args: Array<Any>)

    fun onRequestParams(argName: String, arg: Any)

}