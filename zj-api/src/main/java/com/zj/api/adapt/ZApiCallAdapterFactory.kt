package com.zj.api.adapt

import com.zj.api.ZApi
import com.zj.api.base.BaseCallAdapterFactory
import com.zj.api.call.observable.DefaultAdapterFactory
import com.zj.api.call.coroutine.CoroutineCallAdapter
import com.zj.api.call.coroutine.CoroutineDataCallAdapter
import com.zj.api.call.coroutine.SuspendObservable
import com.zj.api.eh.ApiHandler
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.HandleScheduler
import com.zj.api.mock.Mock
import com.zj.api.mock.MockAble
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.IllegalArgumentException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

open class ZApiCallAdapterFactory<T : Any?>(private val errorHandler: ErrorHandler?, private val mockAble: Boolean) : BaseCallAdapterFactory() {

    private lateinit var pendingData: AdapterPendingData<Any?>

    final override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val mockData = if (mockAble) checkMockService(annotations) else null
        val handleScheduler = getHandleScheduler(annotations)
        pendingData = AdapterPendingData(targetCls, errorHandler, preError, handleScheduler, mockData)
        return getCallAdapter(returnType, annotations, retrofit)
    }

    open fun getCallAdapter(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (!::pendingData.isInitialized) return null
        val funcRawType = getRawType(returnType)
        if (funcRawType != Call::class.java) {
            if (getRawType(returnType) == SuspendObservable::class.java) {
                throw IllegalArgumentException("Functions accessed using coroutines should have their return type com.zj.api.call.coroutine.SuspendObservable<Foo>")
            }
            return DefaultAdapterFactory.createAsync(pendingData).get(returnType, annotations, retrofit)
        }
        check(returnType is ParameterizedType) {
            "return type must be parameterized as SuspendObservable<Foo>"
        }
        val funcType = getParameterUpperBound(0, returnType)
        if (getRawType(funcType) != SuspendObservable::class.java) {
            check(funcType !is Observable<*>) {
                "return type with coroutines just need the final type <Foo> or SuspendObservable<Foo>, example fun request():Int or fun request():SuspendObservable<Int>"
            }
            return CoroutineDataCallAdapter(funcType, pendingData)
        }

        //returnType = suspend observer
        check(funcType is ParameterizedType) {
            "return type must be parameterized as SuspendObservable<Foo>"
        }
        return CoroutineCallAdapter(getParameterUpperBound(0, funcType), pendingData)
    }

    @Suppress("UNCHECKED_CAST")
    private fun checkMockService(annotations: Array<Annotation>): T? {
        val mockAnn = (annotations.find { it is Mock } as? Mock) ?: return null
        val cls: Class<out MockAble> = mockAnn.value.java
        if (cls.typeParameters.isNotEmpty()) {
            throw IllegalArgumentException("Type parameters are unsupported on ${cls.simpleName} constructor!")
        }
        val mock: MockAble = try {
            cls.newInstance()
        } catch (e: Exception) {
            throw IllegalArgumentException("Can not create instance of class ${cls.simpleName} !")
        }
        val mocked = mock.mockData
        return mocked as? T?
    }

    private fun getHandleScheduler(annotations: Array<Annotation>?): HandleScheduler {
        if (annotations == null) return HandleScheduler(ZApi.MAIN, ZApi.MAIN, UUID.randomUUID().toString(), 10 * 1000L)
        val ahi = (annotations.find { it is ApiHandler } as? ApiHandler)
        val scopeSuccess = if (ahi?.successEHScope.isNullOrEmpty()) ZApi.MAIN else ahi!!.successEHScope
        val scopeError = if (ahi?.errorEHScope.isNullOrEmpty()) ZApi.MAIN else ahi!!.errorEHScope
        val id = if (ahi?.id.isNullOrEmpty()) UUID.randomUUID().toString() else ahi!!.id
        val to = ahi?.timeOut ?: 0
        val timeOut = (if (to > 0) to else timeOutDefault).coerceAtLeast(1000)
        if (timeOut != timeOutDefault) {
            timeOutChangeListener?.invoke(timeOut.toInt())
        }
        return HandleScheduler(scopeSuccess, scopeError, id, timeOut)
    }
}