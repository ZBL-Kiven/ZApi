@file:Suppress("unused")

package com.zj.api.downloader

import androidx.lifecycle.LifecycleOwner
import com.zj.api.ZApi
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.LimitScope
import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.LogLevel
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interceptor.plus
import com.zj.api.utils.Constance
import java.io.File
import java.net.URL
import java.util.*

sealed class DownloadBody(
    var lo: LifecycleOwner? = null,
    var errorHandler: ErrorHandler? = null,
    var scheduler: String = ZApi.MAIN,
    var callId: String = UUID.randomUUID().toString(),
    var headers: HeaderProvider? = null,
    var timeout: Long = 30000,
    var listener: DownloadListener? = null,
    var logAble: Boolean = true,
    var logLevel: LogLevel = LogLevel.REQUEST_BODY + LogLevel.HEADERS + LogLevel.SERVER_HEADERS,
) {
    fun <T> result(l: suspend DownloadListener.() -> T?) {
        listener?.let {
            Constance.withScheduler(it, scheduler, lo, l)
        }
    }
}

class DownloadBuilder private constructor(internal val url: UrlProvider, internal var target: File) : DownloadBody() {

    companion object {

        @JvmStatic
        fun with(url: UrlProvider, target: File): DownloadBuilder {
            return DownloadBuilder(url, target)
        }

        @JvmStatic
        fun with(url: URL, target: File): DownloadBuilder {
            return with(url.toString(), target)
        }

        @JvmStatic
        fun with(url: String, target: File): DownloadBuilder {
            return with(UrlProvider.createStatic(url), target)
        }
    }

    internal fun invalid() {
        callId = "-recycled-"
        lo = null
    }

    /**
     * This setting identifies which thread its callback is in. See also [LimitScope]
     * */
    fun observerOn(@LimitScope scheduler: String): DownloadBuilder {
        this.scheduler = scheduler
        return this
    }

    fun header(headers: HeaderProvider?): DownloadBuilder {
        this.headers = headers
        return this
    }

    fun header(vararg h: Pair<String, String?>): DownloadBuilder {
        return header(mutableMapOf(*h))
    }

    fun header(map: Map<String, String?>): DownloadBuilder {
        this.headers = HeaderProvider.createStatic(map)
        return this
    }

    fun callId(cid: String): DownloadBuilder {
        this.callId = cid
        return this
    }

    fun timeout(t: Long): DownloadBuilder {
        this.timeout = t
        return this
    }

    /**
     * Set to bind it to the lifecycle of [LifecycleOwner].、
     * */
    fun bindTo(lo: LifecycleOwner?): DownloadBuilder {
        this.lo = lo
        return this
    }

    /**
     * Ultimately the same effect as [ZApi.create] , @see [ErrorHandler]
     * this handler parses and post the result before it is available , for you to do something.
     * */
    fun errorHandler(handler: ErrorHandler): DownloadBuilder {
        this.errorHandler = handler
        return this
    }

    fun start(listener: DownloadListener? = null): DownloadCompo {
        this.listener = listener
        return DownloadCompo(callId, this)
    }
}