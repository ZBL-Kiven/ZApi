package com.zj.api.adapt

import com.zj.api.base.BaseErrorHandlerObservable
import com.zj.api.interfaces.ErrorHandler
import com.zj.api.utils.Constance
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class CallEnqueueObservableBase<T>(private val originalCall: Call<T?>, errorHandler: ErrorHandler?, private val preError: Throwable?) : BaseErrorHandlerObservable<Response<T?>?>(errorHandler) {

    override fun subscribeActual(observer: Observer<in Response<T?>?>) {
        if (preError != null) {
            Constance.dealExceptionWithEhForObservers(errorHandler, preError, 400, originalCall, observer)
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
                Constance.dealSuccessDataWithEh(errorHandler, response.body()) {
                    observer.onNext(Response.success(response.code(), it))
                }
                if (!disposed) {
                    terminated = true
                    observer.onComplete()
                }
            } catch (t: Throwable) {
                if (terminated) {
                    RxJavaPlugins.onError(t)
                } else if (!disposed) {
                    try {
                        Constance.dealExceptionWithEhForObservers(errorHandler, t, response.code(), call, observer)
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
                Constance.dealExceptionWithEhForObservers(errorHandler, t, 400, call, observer)
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
}
