package com.zj.api.rdt

import com.zj.api.base.BaseRetrofit
import java.lang.annotation.Inherited


const val ALWAYS = 1
const val CLEAR_OBSERVER = 2
const val NEVER = 3

/**
 * @property isRdtEnable : Runtime detection tag ,
 * Whether to enable the runtime state detection capability,
 * enable it when the behavior needs to be automatically cancelled according to the relevant life cycle of [rdtType]
 * */
internal data class RdtMod(val isRdtEnable: Boolean, @RdtType val rdtType: Int, val compo: BaseRetrofit.RequestCompo)

@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class RdtSupport(val token: String, @RdtType val value: Int)