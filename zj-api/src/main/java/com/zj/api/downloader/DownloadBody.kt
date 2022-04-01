@file:Suppress("unused")

package com.zj.api.downloader

import androidx.lifecycle.LifecycleOwner
import com.zj.api.ZApi
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.LimitScope
import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
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
    var downloadInterceptor: DownloadInterceptor? = null,
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

    fun with(lo: LifecycleOwner?): DownloadBuilder {
        this.lo = lo
        return this
    }

    fun errorHandler(handler: ErrorHandler): DownloadBuilder {
        this.errorHandler = handler
        return this
    }

    fun start(listener: DownloadListener? = null, downloadInterceptor: DownloadInterceptor? = null): DownloadCompo {
        this.listener = listener
        this.downloadInterceptor = downloadInterceptor
        return DownloadCompo(this)
    }
}