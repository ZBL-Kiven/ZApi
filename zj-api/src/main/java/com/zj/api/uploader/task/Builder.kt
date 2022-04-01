@file:Suppress("unused")

package com.zj.api.uploader.task

import androidx.lifecycle.LifecycleOwner
import com.zj.api.ZApi
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.LimitScope
import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
import com.zj.api.uploader.FileInfo
import com.zj.api.uploader.FileUploadListener
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

open class UploadBody(
    var lo: LifecycleOwner? = null,
    var errorHandler: ErrorHandler? = null,
    var scheduler: String = ZApi.MAIN,
    var deleteCompressFile: Boolean = false,
    var callId: String = UUID.randomUUID().toString(),
    var headers: HeaderProvider? = null,
    var timeout: Long = 30000,
    var params: MutableMap<String, String?> = mutableMapOf(),
    var contentType: String = "multipart/form-data",
)

@Suppress("unused", "UNCHECKED_CAST")
sealed class Builder<S>(internal val url: UrlProvider) : UploadBody() {

    internal open fun invalid() {
        lo = null
        params.clear()
        callId = "-recycled-"
    }

    open fun contentType(contentType: String): S {
        this@Builder.contentType = contentType
        return this as S
    }

    open fun observerOn(@LimitScope scheduler: String): S {
        this.scheduler = scheduler
        return this as S
    }

    open fun header(headers: HeaderProvider?): S {
        this.headers = headers
        return this as S
    }

    open fun header(vararg h: Pair<String, String?>): S {
        return header(mutableMapOf(*h))
    }

    open fun header(map: Map<String, String?>): S {
        this.headers = HeaderProvider.createStatic(map)
        return this as S
    }

    open fun callId(cid: String): S {
        this.callId = cid
        return this as S
    }

    open fun timeout(t: Long): S {
        this.timeout = t
        return this as S
    }

    open fun with(lo: LifecycleOwner?): S {
        this.lo = lo
        return this as S
    }

    open fun errorHandler(handler: ErrorHandler): S {
        this.errorHandler = handler
        return this as S
    }

    open fun deleteFileAfterUpload(isDelete: Boolean): S {
        this.deleteCompressFile = isDelete
        return this as S
    }

    open fun addParams(params: Map<String, String?>): S {
        this.params.putAll(params)
        return this as S
    }

    open fun addParams(vararg pair: Pair<String, String?>): S {
        this.params.putAll(pair)
        return this as S
    }
}

class UploadBuilder private constructor(url: UrlProvider) : Builder<UploadBuilder>(url) {

    companion object {

        @JvmStatic
        fun with(url: UrlProvider): UploadBuilder {
            return UploadBuilder(url)
        }

        @JvmStatic
        fun with(url: URL): UploadBuilder {
            return with(url.toString())
        }

        @JvmStatic
        fun with(url: String): UploadBuilder {
            return with(UrlProvider.createStatic(url))
        }
    }

    internal var fileInfo: FileInfo? = null

    fun setFileInfo(info: FileInfo): UploadBuilder {
        fileInfo = info
        return this
    }

    fun start(observer: FileUploadListener): SimpleUploadTask {
        return SimpleUploadTask(this, observer)
    }

    override fun invalid() {
        fileInfo = null
        super.invalid()
    }
}

class MultiUploadBuilder private constructor(url: UrlProvider) : Builder<MultiUploadBuilder>(url) {

    companion object {

        @JvmStatic
        fun with(url: UrlProvider): MultiUploadBuilder {
            return MultiUploadBuilder(url)
        }

        @JvmStatic
        fun with(url: URL): MultiUploadBuilder {
            return with(url.toString())
        }

        @JvmStatic
        fun with(url: String): MultiUploadBuilder {
            return with(UrlProvider.createStatic(url))
        }
    }

    internal var files: MutableList<FileInfo>? = mutableListOf()

    fun setFiles(files: List<FileInfo>): MultiUploadBuilder {
        this.files = ArrayList(files)
        return this
    }

    fun addFile(info: FileInfo): MultiUploadBuilder {
        this.files?.add(info)
        return this
    }

    fun addFiles(vararg info: FileInfo): MultiUploadBuilder {
        this.files?.addAll(info)
        return this
    }

    fun start(observer: FileUploadListener): MultiUploadTask {
        return MultiUploadTask(this, observer)
    }
}