package com.zj.api.retrofit

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.HttpException
import retrofit2.Response

internal class BodyObservable<T>(private val upstream: Observable<Response<T>>) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {
        upstream.subscribe(BodyObserver(observer))
    }

    private class BodyObserver<R> constructor(private val observer: Observer<in R>) : Observer<Response<R>> {
        private var terminated: Boolean = false

        override fun onSubscribe(disposable: Disposable) {
            observer.onSubscribe(disposable)
        }

        override fun onNext(response: Response<R>) {
            val body = response.body()
            if (response.isSuccessful && body != null) {
                observer.onNext(body)
            } else {
                terminated = true
                val t = HttpException(response)
                try {
                    observer.onError(t)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }

            }
        }

        override fun onComplete() {
            if (!terminated) {
                observer.onComplete()
            }
        }

        override fun onError(throwable: Throwable) {
            if (!terminated) {
                observer.onError(throwable)
            } else {
                val broken = AssertionError("This should never happen! Report as a bug with the full stacktrace.")
                broken.initCause(throwable)
                RxJavaPlugins.onError(broken)
            }
        }
    }
}
