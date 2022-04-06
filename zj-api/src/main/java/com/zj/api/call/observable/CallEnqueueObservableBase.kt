package com.zj.api.call.observable

import com.zj.api.adapt.AdapterPendingData
import com.zj.api.adapt.HandledException
import com.zj.api.base.BaseErrorHandlerObservable
import com.zj.api.exception.ApiException
import com.zj.api.interfaces.ResponseHandler
import com.zj.api.utils.Constance
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class CallEnqueueObservableBase<T>(private val originalCall: Call<T?>, pendingData: AdapterPendingData<T>) : BaseErrorHandlerObservable<Response<T?>?, T>(pendingData), ResponseHandler<T?, Observer<in Response<T?>?>> {

    override fun subscribeActual(observer: Observer<in Response<T?>?>) {
        if (pendingData.mockData != null) {
            Constance.dealSuccessDataWithEh(pendingData, 200, pendingData.mockData) {
                onSuccess(200, it, observer)
            }
            return
        }
        if (pendingData.preError != null) {
            dealError(originalCall, observer, pendingData.preError)
            return
        }
        val call = originalCall.clone()
        val callback = CallCallback(call, observer)
        observer.onSubscribe(callback)
        call.enqueue(callback)
    }

    inner class CallCallback constructor(private val call: Call<T?>, private val observer: Observer<in Response<T?>?>) : Disposable, Callback<T?> {

        @Volatile private var disposed: Boolean = false
        private var terminated = false

        override fun onResponse(call: Call<T?>, response: Response<T?>) {
            if (disposed) return
            try {
                Constance.parseBodyResponse(pendingData, response, observer, this@CallEnqueueObservableBase)
                if (!disposed) {
                    terminated = true
                    observer.onComplete()
                }
            } catch (t: Throwable) {
                if (terminated) {
                    RxJavaPlugins.onError(t)
                } else if (!disposed) {
                    try {
                        dealError(call, observer, t)
                    } catch (inner: Throwable) {
                        Exceptions.throwIfFatal(inner)
                        RxJavaPlugins.onError(CompositeException(t, inner))
                    }
                }
            }
        }

        override fun onFailure(call: Call<T?>, t: Throwable) {
            if (call.isCanceled) return
            try {
                dealError(call, observer, t)
            } catch (inner: Throwable) {
                Exceptions.throwIfFatal(inner)
                RxJavaPlugins.onError(CompositeException(t, inner))
            }
        }

        override fun dispose() {
            disposed = true
            call.cancel()
        }

        override fun isDisposed(): Boolean {
            return disposed
        }
    }

    override fun onSuccess(code: Int, content: T?, r: Observer<in Response<T?>?>) {
        r.onNext(Response.success(code, content))
    }

    override fun onError(e: ApiException, handled: Any?, r: Observer<in Response<T?>?>) {
        r.onError(HandledException(e, handled))
    }

    private fun dealError(call: Call<T?>, observer: Observer<in Response<T?>?>, t: Throwable) {
        Constance.dealErrorWithEH(pendingData, 400, call, t) { e, a ->
            onError(e, a, observer)
        }
    }
}