@file:Suppress("unused", "UNUSED_PARAMETER")

package com.zj.api

import com.zj.api.base.BaseApiProxy
import com.zj.api.base.BaseRetrofit
import com.zj.api.base.RetrofitFactory
import com.zj.api.interfaces.ErrorHandler
import com.zj.api.utils.TypeUtils
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type

@Suppress("MemberVisibilityCanBePrivate")
class BaseApi<T : Any>(cls: Class<T>, factory: RetrofitFactory<T>?, private val errorHandler: ErrorHandler? = null) : BaseRetrofit<T>(cls, factory) {

    companion object {

        @Suppress("unused")
        @JvmStatic
        inline fun <reified T : Any> create(): BaseApiProxy<T, Nothing> {
            return BaseApiProxy(T::class.java)
        }

        @Suppress("unused")
        @JvmStatic
        inline fun <reified T : Any, reified ERROR_HANDLER : ErrorHandler> createE(): BaseApiProxy<T, ERROR_HANDLER> {
            return BaseApiProxy(T::class.java, ERROR_HANDLER::class.java.newInstance())
        }

        @Suppress("unused")
        @JvmStatic
        inline fun <reified T : Any> create(handler: ErrorHandler): BaseApiProxy<T, ErrorHandler> {
            return BaseApiProxy(T::class.java, handler)
        }

        @JvmStatic
        fun <T : Any> create(cls: Class<T>): BaseApiProxy<T, *> {
            return BaseApiProxy<T, Nothing>(cls)
        }
    }

    fun <R> zip(observer: (T) -> Array<Observable<*>>, onFunc: (Any, Any, Any) -> R, subscribe: ((isSuccess: Boolean, data: R, throwable: HttpException?) -> Unit)? = null) {

        val observables = observer.invoke(getService())
        val obsMap = mutableMapOf<String, Observable<*>>()
        observables.forEach {
            val t: Type = TypeUtils.getFirstClassType(it::class.java)
            val typeName = t.toString()
            obsMap[typeName] = it
        }

    }

    fun <F> request(observer: (T) -> Observable<F>, subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?) -> Unit)? = null) {
        val subscribeSchedulers: Scheduler = Schedulers.io()
        val observableSchedulers: Scheduler = AndroidSchedulers.mainThread()
        this.request(observer, subscribeSchedulers, observableSchedulers, subscribe)
    }

    fun <F> call(observer: (T) -> Observable<F>, subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?) -> Unit)? = null): RequestCompo {
        val subscribeSchedulers: Scheduler = Schedulers.io()
        val observableSchedulers: Scheduler = AndroidSchedulers.mainThread()
        return this.call(observer, subscribeSchedulers, observableSchedulers, subscribe)
    }

    fun <F> request(observer: (T) -> Observable<F>, subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?) -> Unit)? = null) {
        RequestInCompo(observer(getService()), subscribeSchedulers, observableSchedulers, { data ->
            subscribe?.invoke(true, data, null)
        }, { throwable ->
            throwable?.let {
                errorHandler?.onError(it)
            }
            val thr = throwable as? HttpException
            subscribe?.invoke(thr?.code() == 204, null, thr)
        }).init()
    }

    fun <F> call(observer: (T) -> Observable<F>, subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?) -> Unit)? = null): RequestCompo {
        val requestInCompo: RequestInCompo<F>?
        requestInCompo = RequestInCompo(observer(getService()), subscribeSchedulers, observableSchedulers, { data ->
            subscribe?.invoke(true, data, null)
        }, { throwable ->
            throwable?.let {
                errorHandler?.onError(it)
            }
            val thr = throwable as? HttpException
            subscribe?.invoke(thr?.code() == 204, null, thr)
        })
        requestInCompo.init()
        return object : RequestCompo {
            override fun cancel() {
                requestInCompo.cancel()
            }
        }
    }
}