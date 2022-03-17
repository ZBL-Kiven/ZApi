package com.zj.api.base

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException


class RequestInCompo<F>(private val observer: Observable<F>, private val subscribeSchedulers: Scheduler, private val observableSchedulers: Scheduler, private val subscribe: ((F?) -> Unit)? = null, private val exception: ((throwable: Throwable) -> Unit)? = null) {

    companion object {

        internal val compo = CompositeDisposable()

        @Suppress("unused")
        fun shutDown() {
            compo.dispose()
            compo.clear()
        }
    }

    private var disposable: Disposable? = null
    private var cancelAble = false

    internal fun init(): RequestInCompo<F> {
        disposable = observer.subscribeOn(subscribeSchedulers).observeOn(observableSchedulers).subscribe({ data ->
            if (cancelAble) return@subscribe
            subscribe?.invoke(data)
            disposable?.let { compo.remove(it) }
        }, { throwable ->
            if (cancelAble) return@subscribe
            exception?.invoke(throwable)
            disposable?.let { compo.remove(it) }
        })
        disposable?.let { compo.add(it) }
        return this@RequestInCompo
    }

    fun cancel() {
        cancelAble = true
        disposable?.let {
            it.dispose()
            compo.remove(it)
        }
    }
}