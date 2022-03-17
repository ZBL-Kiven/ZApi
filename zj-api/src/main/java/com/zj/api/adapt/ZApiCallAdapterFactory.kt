package com.zj.api.adapt

import com.zj.api.base.BaseCallAdapterFactory
import com.zj.api.coroutine.CoroutineCallAdapter
import com.zj.api.coroutine.CoroutineDataCallAdapter
import com.zj.api.coroutine.SuspendObservable
import com.zj.api.interfaces.ErrorHandler
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.IllegalArgumentException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class ZApiCallAdapterFactory<T : Any?>(private val errorHandler: ErrorHandler?) : BaseCallAdapterFactory() {

    final override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        return getCallAdapter(returnType, annotations, retrofit)
    }

    open fun getCallAdapter(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val funcRawType = getRawType(returnType)
        if (funcRawType != Call::class.java) {
            if (getRawType(returnType) == SuspendObservable::class.java) {
                throw IllegalArgumentException("Functions accessed using coroutines should have their return type com.zj.api.coroutine.SuspendObservable<Foo>")
            }
            return DefaultAdapterFactory.createAsync(errorHandler, preError).get(returnType, annotations, retrofit)
        }
        check(returnType is ParameterizedType) {
            "return type must be parameterized as SuspendObservable<Foo>"
        }
        val funcType = getParameterUpperBound(0, returnType)
        if (getRawType(funcType) != SuspendObservable::class.java) {
            check(funcType !is Observable<*>) {
                "return type with coroutines just need the final type <Foo> or SuspendObservable<Foo>, example fun request():Int or fun request():SuspendObservable<Int>"
            }
            return CoroutineDataCallAdapter<T>(funcType, errorHandler, preError)
        }

        //returnType = suspend observer
        check(funcType is ParameterizedType) {
            "return type must be parameterized as SuspendObservable<Foo>"
        }
        return CoroutineCallAdapter<T>(getParameterUpperBound(0, funcType), errorHandler, preError)
    }
}