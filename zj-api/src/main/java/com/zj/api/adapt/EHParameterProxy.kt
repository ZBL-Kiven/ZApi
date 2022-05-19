package com.zj.api.adapt

import com.zj.api.eh.EHParams
import com.zj.ok3.MethodHandler
import com.zj.ok3.ZHttpServiceCreator
import java.lang.reflect.Method

internal class EHParameterProxy<T>(private val hsc: ZHttpServiceCreator, private val cls: Class<T>, private val lazyParamFinder: (Boolean, MutableMap<String, Any?>) -> Unit) : MethodHandler {

    companion object {

        fun <T> create(hsc: ZHttpServiceCreator, cls: Class<T>, lazyParamFinder: (Boolean, MutableMap<String, Any?>) -> Unit): T {
            return EHParameterProxy(hsc, cls, lazyParamFinder).onProxy()
        }
    }

    fun onProxy(): T {
        return hsc.create(cls, this)
    }

    override fun parseParameterMaps(method: Method, args: Array<Any>) {
        val paramsMap = mutableMapOf<String, Any?>()
        lazyParamFinder.invoke(true, paramsMap)
        val annotations = method.parameterAnnotations
        annotations.forEachIndexed { i, v ->
            val ehp = v.firstOrNull { it is EHParams }
            if (ehp != null) {
                val value = args[i]
                paramsMap[(ehp as EHParams).value] = value
            }
        }
        lazyParamFinder.invoke(false, paramsMap)
    }

    override fun onRequestParams(argName: String, arg: Any) {
        lazyParamFinder.invoke(false, mutableMapOf(Pair(argName, arg)))
    }
}