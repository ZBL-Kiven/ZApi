package com.zj.api.call.observable

import com.zj.api.adapt.AdapterPendingData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit

@Suppress("unused")
internal class DefaultAdapterFactory private constructor(private val scheduler: Scheduler?, private val isAsync: Boolean, private val pendingData: AdapterPendingData<Any?>) : CallAdapter.Factory() {


    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        if (rawType == Completable::class.java) {
            return DefaultCallAdapter(Void::class.java, scheduler, isAsync, isResult = false, isBody = true, isFlowAble = false, isSingle = false, isMaybe = false, isCompletable = true, pendingData)
        }
        val isFlowAble = rawType == Flowable::class.java
        val isSingle = rawType == Single::class.java
        val isMaybe = rawType == Maybe::class.java
        if (rawType != Observable::class.java && !isFlowAble && !isSingle && !isMaybe) {
            return null
        }

        var isResult = false
        var isBody = false
        val responseType: Type
        if (returnType !is ParameterizedType) {
            val name = when {
                isFlowAble -> "FlowAble"
                isSingle -> "Single"
                isMaybe -> "Maybe"
                else -> "Observable"
            }
            throw IllegalStateException("$name return type must be parameterized as $name<Foo> or $name<? extends Foo>")
        }

        val observableType = getParameterUpperBound(0, returnType)
        when (getRawType(observableType)) {
            Response::class.java -> {
                if (observableType !is ParameterizedType) {
                    throw IllegalStateException("Response must be parameterized" + " as Response<Foo> or Response<? extends Foo>")
                }
                responseType = getParameterUpperBound(0, observableType)
            }
            Result::class.java -> {
                if (observableType !is ParameterizedType) {
                    throw IllegalStateException("Result must be parameterized" + " as Result<Foo> or Result<? extends Foo>")
                }
                responseType = getParameterUpperBound(0, observableType)
                isResult = true
            }
            else -> {
                responseType = observableType
                isBody = true
            }
        }
        return DefaultCallAdapter(responseType, scheduler, isAsync, isResult, isBody, isFlowAble, isSingle, isMaybe, false, pendingData)
    }

    companion object {
        /**
         * Returns an instance which creates synchronous observables that do not operate on any scheduler
         * by default.
         */
        fun create(pendingData: AdapterPendingData<Any?>): DefaultAdapterFactory {
            return DefaultAdapterFactory(null, false, pendingData)
        }

        /**
         * Returns an instance which creates asynchronous observables. Applying
         * [Observable.subscribeOn] has no effect on stream types created by this factory.
         */
        fun createAsync(pendingData: AdapterPendingData<Any?>): DefaultAdapterFactory {
            return DefaultAdapterFactory(null, true, pendingData)
        }

        /**
         * Returns an instance which creates synchronous observables that
         * [subscribe on][Observable.subscribeOn] `scheduler` by default.
         */

        fun createWithScheduler(scheduler: Scheduler?, pendingData: AdapterPendingData<Any?>): DefaultAdapterFactory {
            if (scheduler == null) throw NullPointerException("scheduler == null")
            return DefaultAdapterFactory(scheduler, false, pendingData)
        }
    }
}
