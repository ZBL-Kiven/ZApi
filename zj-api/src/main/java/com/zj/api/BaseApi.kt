@file:Suppress("unused")

package com.zj.api

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.zj.api.adapt.HandledException
import com.zj.api.base.BaseApiProxy
import com.zj.api.base.RequestInCompo
import com.zj.api.downloader.DownloadCompo
import com.zj.api.downloader.DownloadListener
import com.zj.api.interfaces.ErrorHandler
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.utils.LogUtils
import com.zj.api.utils.LoggerInterface
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.File
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ZApi {

    companion object : Consumer<Throwable> {

        fun setLoggerInterface(cls: Class<*>, lin: LoggerInterface) {
            LogUtils.setByteSizeListener(cls, lin)
        }

        init {
            RxJavaPlugins.setErrorHandler(this)
        }

        @JvmStatic
        fun <T : Any> create(cls: Class<T>): BaseApiProxy<T, *> {
            return BaseApiProxy<T, Nothing>(cls)
        }

        @Suppress("unused")
        @JvmStatic
        inline fun <reified T : Any> create(): BaseApiProxy<T, Nothing> {
            return BaseApiProxy(T::class.java)
        }

        @Suppress("unused")
        @JvmStatic
        fun <T : Any, ERROR_HANDLER : ErrorHandler> create(cls: Class<T>, handler: ERROR_HANDLER): BaseApiProxy<T, ErrorHandler> {
            return BaseApiProxy(cls, handler)
        }

        @Suppress("unused")
        @JvmStatic
        inline fun <reified T : Any, ERROR_HANDLER : ErrorHandler> create(handler: ERROR_HANDLER): BaseApiProxy<T, ErrorHandler> {
            return BaseApiProxy(T::class.java, handler)
        }

        @Suppress("unused")
        @JvmStatic
        inline fun <reified T : Any, reified ERROR_HANDLER : ErrorHandler> createE(): BaseApiProxy<T, ERROR_HANDLER> {
            return BaseApiProxy(T::class.java, ERROR_HANDLER::class.java.newInstance())
        }

        fun download(target: File, url: String, listener: DownloadListener): DownloadCompo {
            return DownloadCompo(target, url, listener)
        }

        override fun accept(t: Throwable?) {
            t?.printStackTrace()
        }
    }
}


fun <F> Observable<F>.call(lo: LifecycleOwner?, subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?, errorHandlerResp: Any?) -> Unit)? = null): RequestCancelable {
    return call(lo, Schedulers.io(), AndroidSchedulers.mainThread(), subscribe)
}

fun <F> Observable<F>.call(lo: LifecycleOwner?, subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?) -> Unit)? = null): RequestCancelable {
    return call(lo, Schedulers.io(), AndroidSchedulers.mainThread()) { isSuccess: Boolean, data: F?, throwable: HttpException?, _ ->
        subscribe?.invoke(isSuccess, data, throwable)
    }
}

fun <F> Observable<F>.call(subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?) -> Unit)? = null): RequestCancelable {
    return call(null, subscribeSchedulers, observableSchedulers) { isSuccess: Boolean, data: F?, throwable: HttpException?, _ ->
        subscribe?.invoke(isSuccess, data, throwable)
    }
}

fun <F> Observable<F>.call(subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?, errorHandlerResp: Any?) -> Unit)? = null): RequestCancelable {
    return call(null, subscribeSchedulers, observableSchedulers, subscribe)
}

fun <F> Observable<F>.call(lo: LifecycleOwner?, subscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread(), subscribe: ((isSuccess: Boolean, data: F?, throwable: HttpException?, errorHandlerResp: Any?) -> Unit)? = null): RequestCancelable {

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

private fun <F> dealError(throwable: Throwable?, subscribe: ((Boolean, F?, HttpException?, Any?) -> Unit)?) {
    var er = throwable
    var hd: Any? = null
    if (throwable is HandledException) {
        er = throwable.raw
        hd = throwable.handledData
    }
    if (er is HttpException) {
        subscribe?.invoke(er.code() == 204, null, er, hd)
    }
}