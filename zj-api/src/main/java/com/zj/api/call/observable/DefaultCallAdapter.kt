package com.zj.api.call.observable

import com.zj.api.adapt.AdapterPendingData
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.lang.reflect.Type
import com.zj.ok3.Call
import com.zj.ok3.CallAdapter

internal class DefaultCallAdapter<R>(private val responseType: Type, private val scheduler: Scheduler?, private val isAsync: Boolean, private val isResult: Boolean, private val isBody: Boolean, private val isFlowAble: Boolean, private val isSingle: Boolean, private val isMaybe: Boolean, private val isCompletable: Boolean, private val pendingData: AdapterPendingData<R>) : CallAdapter<R?, Any> {

    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<R?>): Any {
        val responseObservable = if (isAsync) CallEnqueueObservableBase(call, pendingData) else CallExecuteObservableBase(call, pendingData)
        var observable: Observable<*>
        observable = when {
            isResult -> ResultObservable(responseObservable)
            isBody -> BodyObservable(responseObservable)
            else -> responseObservable
        }

        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler)
        }

        if (isFlowAble) {
            return observable.toFlowable(BackpressureStrategy.LATEST)
        }
        if (isSingle) {
            return observable.singleOrError()
        }
        if (isMaybe) {
            return observable.singleElement()
        }
        return if (isCompletable) {
            observable.ignoreElements()
        } else observable
    }
}
