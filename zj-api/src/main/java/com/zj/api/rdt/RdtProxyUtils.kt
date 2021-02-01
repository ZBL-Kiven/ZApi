package com.zj.api.rdt

import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object RdtProxyUtils {

    private val cocMap = ConcurrentHashMap<String, RdtMod>()

    internal fun ArrayList<RdtMod>.sendToProxy(callingId: String) {
        this.forEach { cocMap[callingId] = it }
    }


    fun parseAnnotation(annotations: Array<Annotation>) {

    }


    private var callingId: String? = null
        set(value) {
            if (!field.isNullOrEmpty() && !value.isNullOrEmpty()) return
            field = value
        }

//    init {
//        try {
//            //            callingId = UUID.randomUUID().toString()
//            //            val methods = cls.declaredMethods
//            //            val annotations = methods.mapNotNullTo(arrayListOf()) { it.getAnnotation(RdtSupport::class.java) }
//            //            annotations.forEach {
//            //
//            //            }
//        } catch (e: Exception) {
//
//
//        }
//    }
}
