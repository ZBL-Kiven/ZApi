package com.zj.api.uploader

import androidx.lifecycle.LifecycleOwner
import com.zj.api.ZApi
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.LimitScope
import com.zj.api.interceptor.UrlProvider
import com.zj.api.uploader.task.MultiUploadTask
import com.zj.api.uploader.task.UploadTask
import io.reactivex.Observable
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@Suppress("MemberVisibilityCanBePrivate", "unused")
sealed class Builder<T>(internal val url: URL, internal val observable: Observable<T>) {

    internal var lo: LifecycleOwner? = null
    internal var errorHandler: ErrorHandler? = null
    internal var scheduler: String = ZApi.MAIN
    internal var deleteCompressFile: Boolean = false
    internal var callId: String = UUID.randomUUID().toString()
    internal var headers: MutableMap<String, String>? = null
    internal var params: Map<String, String?>? = null
    internal var contentType: String = "multipart/form-data"
    internal lateinit var uploadTask: UploadTask

    fun contentType(contentType: String): Builder<T> {
        this.contentType = contentType
        return this
    }

    fun subscribeOn(@LimitScope scheduler: String): Builder<T> {
        this.scheduler = scheduler
        return this
    }

    fun addHeader(headers: MutableMap<String, String>): Builder<T> {
        this.headers = headers
        return this
    }

    fun callId(cid: String): Builder<T> {
        this.callId = cid
        return this
    }

    fun with(lo: LifecycleOwner?): Builder<T> {
        this.lo = lo
        return this
    }

    fun setErrorHandler(handler: ErrorHandler): Builder<T> {
        this.errorHandler = handler
        return this
    }

    fun deleteFileAfterUpload(isDelete: Boolean): Builder<T> {
        this.deleteCompressFile = isDelete
        return this
    }

    fun addParams(params: Map<String, String?>?): Builder<T> {
        this.params = params
        return this
    }

    internal open fun invalid() {
        headers?.clear()
        params = null
        callId = "-recycled-"
    }
}

/**
 * 大多数情况下兼容使用，除非你使用非 Post 协议执行上传，或需要批量上传。
 * */
class UploadBuilder<T> : Builder<T> {



    internal constructor(url: UrlProvider, observable: Observable<T>) : super(URL(url.url()), observable)

    internal constructor(url: String, observable: Observable<T>) : super(URL(url), observable)

    internal var fileInfo: FileInfo? = null

    fun setFileInfo(info: FileInfo): Builder<T> {
        fileInfo = info
        return this
    }

    fun start(observer: FileUploadListener): UploadTask {
        uploadTask = UploadTask(this, observer)
        return uploadTask
    }

    override fun invalid() {
        fileInfo = null
        super.invalid()
    }
}


class MultiUploadBuilder<T> : Builder<T> {

    internal constructor(url: URL, observable: Observable<T>) : super(url, observable)

    internal constructor(url: UrlProvider, observable: Observable<T>) : super(URL(url.url()), observable)

    internal constructor(url: String, observable: Observable<T>) : super(URL(url), observable)

    internal var files: MutableList<FileInfo>? = mutableListOf()

    fun setFiles(files: List<FileInfo>): Builder<T> {
        this.files = ArrayList(files)
        return this
    }

    fun addFile(info: FileInfo): Builder<T> {
        this.files?.add(info)
        return this
    }

    fun addFiles(vararg info: FileInfo): Builder<T> {
        this.files?.addAll(info)
        return this
    }

    fun start(observer: FileUploadListener): MultiUploadTask {
        uploadTask = MultiUploadTask(this, observer)
        return uploadTask
    }
}