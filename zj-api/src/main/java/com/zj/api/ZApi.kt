@file:Suppress("unused")

package com.zj.api

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.zj.api.adapt.HandledException
import com.zj.api.base.BaseApiProxy
import com.zj.api.base.RequestInCompo
import com.zj.api.downloader.DownloadBuilder
import com.zj.api.eh.ErrorHandler
import com.zj.api.exception.ApiException
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.uploader.task.MultiUploadBuilder
import com.zj.api.uploader.task.UploadBuilder
import com.zj.api.utils.LogUtils
import com.zj.api.utils.LoggerInterface
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ZApi {

    companion object : Consumer<Throwable> {

        const val MAIN = "mainScope"
        const val IO = "ioScope"
        const val CALCULATE = "calculate"
        internal const val mBaseTimeoutMills = 10 * 1000
        internal const val mBaseReadTimeoutMills = 30 * 1000
        internal const val mBaseWriteTimeoutMills = 30 * 1000

        init {
            RxJavaPlugins.setErrorHandler(this)
        }

        /**
         * Add traffic and log monitors to the class to which the execution function belongs.
         * @param cls All Interfaces contained within it will be added to the traffic monitoring system during dynamic proxy resolution.
         * */
        fun addFlowsListener(cls: Class<*>, lin: LoggerInterface) {
            LogUtils.addStreamingListener(cls, lin)
        }

        /**
         * set a global traffic and log monitors
         * */
        fun setGlobalFlowsListener(lin: LoggerInterface) {
            LogUtils.setGlobalStreamingListener(lin)
        }

        /**
         * --------------------------------------------------- Create your service ⬇  ----------------------------------------------------------------
         * */
        @JvmStatic
        fun <T : Any> create(cls: Class<T>): BaseApiProxy<T, *> {
            return BaseApiProxy<T, Nothing>(cls)
        }

        @JvmStatic
        inline fun <reified T : Any> create(): BaseApiProxy<T, Nothing> {
            return BaseApiProxy(T::class.java)
        }

        @JvmStatic
        fun <T : Any, ERROR_HANDLER : ErrorHandler> create(cls: Class<T>, handler: ERROR_HANDLER?): BaseApiProxy<T, ErrorHandler> {
            return BaseApiProxy(cls, handler)
        }

        @JvmStatic
        inline fun <reified T : Any, ERROR_HANDLER : ErrorHandler> create(handler: ERROR_HANDLER?): BaseApiProxy<T, ErrorHandler> {
            return BaseApiProxy(T::class.java, handler)
        }

        @JvmStatic
        inline fun <reified T : Any, reified ERROR_HANDLER : ErrorHandler> createE(): BaseApiProxy<T, ERROR_HANDLER> {
            return BaseApiProxy(T::class.java, ERROR_HANDLER::class.java.newInstance())
        }

        /**
         * --------------------------------------------------- Create your service ⬆ ----------------------------------------------------------------
         * */

        val Uploader = UploadBuilder.Companion

        val MultiUploader = MultiUploadBuilder.Companion

        val Downloader = DownloadBuilder.Companion

        override fun accept(t: Throwable?) {
            t?.printStackTrace()
        }
    }
}

/**
 * Observable uses the entry, see README.md for details
 *
 * @param subscribe#fromErrorHandler see [ErrorHandler]
 *
 * @return see [RequestCancelable]
 * */
fun <F> Observable<F>.call(lo: LifecycleOwner?, subscribe: ((isSuccess: Boolean, data: F?, throwable: ApiException?, fromErrorHandler: Any?) -> Unit)? = null): RequestCancelable {
    return call(lo, Schedulers.io(), AndroidSchedulers.mainThread(), subscribe)
}

fun <F> Observable<F>.call(lo: LifecycleOwner?, subscribe: ((isSuccess: Boolean, data: F?, throwable: ApiException?) -> Unit)? = null): RequestCancelable {
    return call(lo, Schedulers.io(), AndroidSchedulers.mainThread()) { isSuccess: Boolean, data: F?, throwable: ApiException?, _ ->
        subscribe?.invoke(isSuccess, data, throwable)
    }
}

fun <F> Observable<F>.call(subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: ApiException?) -> Unit)? = null): RequestCancelable {
    return call(null, subscribeSchedulers, observableSchedulers) { isSuccess: Boolean, data: F?, throwable: ApiException?, _ ->
        subscribe?.invoke(isSuccess, data, throwable)
    }
}

fun <F> Observable<F>.call(subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: ApiException?, fromErrorHandler: Any?) -> Unit)? = null): RequestCancelable {
    return call(null, subscribeSchedulers, observableSchedulers, subscribe)
}

/**
 * Observable uses the entry, see README.md for details
 *
 * @param [observableSchedulers] [subscribeSchedulers] Subscribing to thread schedulers such as observation, the purpose of retaining this method is to be compatible with earlier ZApi versions and used by RxAndroid + Observable。
 *
 * @param lo If it exists, this execution function will be bound to its life cycle。
 * */
fun <F> Observable<F>.call(lo: LifecycleOwner?, subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: ApiException?, fromErrorHandler: Any?) -> Unit)? = null): RequestCancelable {

    suspend fun suspendReq() = suspendCancellableCoroutine<F?> { scc ->
        RequestInCompo(this, subscribeSchedulers, observableSchedulers, { data ->
            scc.resume(data)
        }, { throwable ->
            scc.resumeWithException(throwable)
        }).init()
    }

    fun doReq(): RequestInCompo<F> {
        return RequestInCompo(this, subscribeSchedulers, observableSchedulers, { data ->
            subscribe?.invoke(true, data, null, null)
        }, { throwable ->
            dealError(throwable, subscribe)
        }).init()
    }
    return lo?.lifecycleScope?.launch {
        try {
            val data = suspendReq()
            subscribe?.invoke(true, data, null, null)
        } catch (e: Exception) {
            dealError(e, subscribe)
        }
    }?.let {
        object : RequestCancelable {
            override fun cancel(msg: String?, throwable: Throwable?) {
                it.cancel(msg ?: "", throwable)
            }
        }
    } ?: (doReq().let {
        object : RequestCancelable {
            override fun cancel(msg: String?, throwable: Throwable?) {
                it.cancel()
            }
        }
    })
}

private fun <F> dealError(throwable: Throwable?, subscribe: ((Boolean, F?, ApiException?, Any?) -> Unit)?) {
    var er = throwable
    var hd: Any? = null
    if (throwable is HandledException) {
        er = throwable.raw
        hd = throwable.handledData
    }
    if (er is ApiException) {
        subscribe?.invoke(er.httpException?.code() == 204, null, er, hd)
    }
}