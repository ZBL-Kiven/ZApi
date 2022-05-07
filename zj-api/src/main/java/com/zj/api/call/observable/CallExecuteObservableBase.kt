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
import com.zj.ok3.Call
import com.zj.ok3.Response

internal class CallExecuteObservableBase<T>(private val originalCall: Call<T?>, pendingData: AdapterPendingData<T>) : BaseErrorHandlerObservable<Response<T?>?, T>(pendingData), ResponseHandler<T?, Observer<in Response<T?>?>> {

    override fun subscribeActual(observer: Observer<in Response<T?>?>) {
        if (pendingData.mockData != null) {
            Constance.dealSuccessDataWithEh(pendingData, 200, pendingData.mockData.getMockData(pendingData.methodParamData)) {
                onSuccess(200, it, observer)
            }
            return
        }
        if (pendingData.preError != null) {
            Constance.dealErrorWithEH(pendingData, 400, originalCall, pendingData.preError) { e, a ->
                onError(e, a, observer)
            }
            return
        }
        val call = originalCall.clone()
        val disposable = CallDisposable(call)
        observer.onSubscribe(disposable)
        var terminated = false
        try {
            val response = call.execute()
            if (!disposable.isDisposed) {
                Constance.parseBodyResponse(pendingData, response, observer, this)
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
                    Constance.dealErrorWithEH(pendingData, 400, call, t) { e, a ->
                        onError(e, a, observer)
                    }
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

    override fun onSuccess(code: Int, content: T?, r: Observer<in Response<T?>?>) {
        r.onNext(Response.success(code, content))
    }

    override fun onError(e: ApiException, handled: Any?, r: Observer<in Response<T?>?>) {
        r.onError(HandledException(e, handled))
    }
}
