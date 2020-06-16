package com.zj.api.utils

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@Suppress("MemberVisibilityCanBePrivate", "unused")
internal object TypeUtils {


    fun getFirstClassType(cls: Class<*>): Type {
        return getClassTypes(cls)[0]
    }

    fun getClassTypes(cls: Class<*>): Array<Type> {
        val pt: ParameterizedType = cls.genericInterfaces[0] as ParameterizedType
        return pt.actualTypeArguments
    }

}
