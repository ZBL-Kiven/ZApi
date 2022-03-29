package com.zj.api.call.observable

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.Response

internal class ResultObservable<T>(private val upstream: Observable<Response<T?>?>) : Observable<Result<T?>?>() {

    override fun subscribeActual(observer: Observer<in Result<T?>?>) {
        upstream.subscribe(ResultObserver(observer))
    }

    private class ResultObserver<R> constructor(private val observer: Observer<in Result<R?>?>) : Observer<Response<R?>?> {

        override fun onSubscribe(disposable: Disposable) {
            observer.onSubscribe(disposable)
        }

        override fun onNext(response: Response<R?>) {
            observer.onNext(Result.response(response))
        }

        override fun onError(throwable: Throwable) {
            try {
                observer.onNext(Result.error(throwable))
            } catch (t: Throwable) {
                try {
                    observer.onError(t)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }

                return
            }

            observer.onComplete()
        }

        override fun onComplete() {
            observer.onComplete()
        }
    }
}
