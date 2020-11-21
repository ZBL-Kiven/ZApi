package com.zj.api.base

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseRetrofit<T>(private val cls: Class<T>, private val retrofitFactory: RetrofitFactory<T>) {

    protected fun getService(): T? {
        return retrofitFactory.let {
            if (it.valuable) it.createService(cls)
            else null
        }
    }

    /** ------  RequestInCompo ------ **/

    companion object {

        internal val compo = CompositeDisposable()

        @Suppress("unused")
        fun shutDown() {
            compo.dispose()
            compo.clear()
        }
    }

    class RequestInCompo<F>(private val observer: Observable<F>, private val subscribeSchedulers: Scheduler, private val observableSchedulers: Scheduler, private val subscribe: ((F?) -> Unit)? = null, private val exception: ((throwable: Throwable?) -> Unit)? = null) {

        private var disposable: Disposable? = null
        private var cancelAble = false

        internal fun init() {
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
        }

        fun cancel() {
            cancelAble = true
            disposable?.let {
                it.dispose()
                compo.remove(it)
            }
        }
    }

    interface RequestCompo {
        fun cancel()
    }
}