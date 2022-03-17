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
import retrofit2.Response

internal class CallExecuteObservableBase<T>(private val originalCall: Call<T?>, errorHandler: ErrorHandler?, private val preError: Throwable?) : BaseErrorHandlerObservable<Response<T?>?>(errorHandler) {

    override fun subscribeActual(observer: Observer<in Response<T?>?>) {
        if (preError != null) {
            Constance.dealExceptionWithEhForObservers(errorHandler, preError, 400, originalCall, observer)
            return
        }
        val call = originalCall.clone()
        val disposable = CallDisposable(call)
        observer.onSubscribe(disposable)

        var terminated = false
        try {
            val response = call.execute()
            if (!disposable.isDisposed) {
                Constance.dealSuccessDataWithEh(errorHandler, response.body()) {
                    observer.onNext(Response.success(response.code(), it))
                }
            }
            if (!disposable.isDisposed) {
                terminated = true
                observer.onComplete()
            }
        } catch (t: Throwable) {
            Exceptions.throwIfFatal(t)
            if (terminated) {
                RxJavaPlugins.onError(t)
            } else if (!disposable.isDisposed) {
                try {
                    Constance.dealExceptionWithEhForObservers(errorHandler, t, 400, call, observer)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }
            }
        }
    }

    private class CallDisposable constructor(private val call: Call<*>) : Disposable {
        @Volatile private var disposed: Boolean = false

        override fun dispose() {
            disposed = true
            call.cancel()
        }

        override fun isDisposed(): Boolean {
            return disposed
        }
    }
}
