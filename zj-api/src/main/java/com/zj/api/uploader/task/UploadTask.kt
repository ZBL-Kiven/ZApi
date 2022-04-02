package com.zj.api.uploader.task

import com.zj.api.ZApi
import com.zj.api.call
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.progress.ProgressListener
import com.zj.api.uploader.FileInfo
import com.zj.api.uploader.FileUploadListener
import com.zj.api.uploader.UploadInterceptor
import com.zj.api.uploader.ZUploadService
import com.zj.api.utils.Constance
import com.zj.api.utils.LogUtils
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@Suppress("SpellCheckingInspection", "unused")
sealed class UploadTask<X : Builder<*>>(protected val builder: X, protected val observer: FileUploadListener?, private val uploadInterceptor: UploadInterceptor, val callId: String = builder.callId) : ProgressListener {

    protected var reqCompo: RequestCancelable? = null
    protected var totalBytes: Long = 0

    abstract fun startUpload(url: String, api: ZUploadService, subscribeOn: Scheduler)

    init {
        upload()
    }

    private fun upload() {
        val subscribeOn = when (builder.scheduler) {
            ZApi.CALCULATE -> Schedulers.computation()
            ZApi.IO -> Schedulers.io()
            else -> AndroidSchedulers.mainThread()
        }
        if (!uploadInterceptor.intercept(builder, observer)) {
            val url = builder.url.url()
            val api = ZApi.create(ZUploadService::class.java, builder.errorHandler).header(builder.headers).timeOut(builder.timeout).build()
            startUpload(url, api, subscribeOn)
        }
    }

    fun cancel() {
        observer?.onError(builder.callId, null, Constance.parseOrCreateHttpException(builder.callId, builder.url.toString(), builder.params, InterruptedException("canceled!!")), null)
        reqCompo?.cancel()
    }

    fun destroy() {
        builder.invalid()
        reqCompo?.cancel()
    }

    override fun onProgress(info: FileInfo, progress: Int, contentLength: Long) {
        this.totalBytes = contentLength
        observer?.onProgress(builder.callId, info, progress, contentLength)
    }

    protected fun delete(path: String?) {
        if (path.isNullOrEmpty()) return
        val file = File(path)
        if (file.exists() && file.delete()) {
            LogUtils.d("FileUploadTask", "the temp file $path has delete success!")
        } else {
            LogUtils.d("FileUploadTask", "the temp file $path was delete failed!")
        }
    }
}

class SimpleUploadTask @JvmOverloads internal constructor(builder: UploadBuilder, observer: FileUploadListener? = null, uploadInterceptor: UploadInterceptor = UploadInterceptor.getDefault()) : UploadTask<UploadBuilder>(builder, observer, uploadInterceptor) {

    override fun startUpload(url: String, api: ZUploadService, subscribeOn: Scheduler) {
        val fileInfo = builder.fileInfo ?: return
        val f = File(fileInfo.path)
        val rq = RequestBody.create(MediaType.parse(builder.contentType), f)
        val progressBody = ProgressRequestBody(rq, fileInfo, this)
        val part = MultipartBody.Part.createFormData(fileInfo.paramName, fileInfo.name, progressBody)
        val map = mutableMapOf<String, RequestBody>()
        builder.params.forEach { (s, s1) ->
            val paramsPart = RequestBody.create(MediaType.parse(builder.contentType), s1 ?: "")
            map[s] = paramsPart
        }
        reqCompo = api.upload(url, map, part).call(builder.lo, Schedulers.io(), subscribeOn) { isSuccess, data, throwable, a ->
            if (isSuccess) {
                observer?.onSuccess(builder.callId, data, totalBytes)
            } else {
                observer?.onError(builder.callId, fileInfo, throwable, a)
            }
            observer?.onCompleted(builder.callId, fileInfo, totalBytes)
            destroy()
        }
    }

    override fun onComplete(info: FileInfo, contentLength: Long) {
        if (builder.deleteCompressFile) delete(info.path)
        observer?.onUploaded(builder.callId, info, contentLength)
    }
}

class MultiUploadTask @JvmOverloads internal constructor(builder: MultiUploadBuilder, observer: FileUploadListener? = null, uploadInterceptor: UploadInterceptor = UploadInterceptor.getDefault()) : UploadTask<MultiUploadBuilder>(builder, observer, uploadInterceptor) {

    override fun startUpload(url: String, api: ZUploadService, subscribeOn: Scheduler) {
        val body = MultipartBody.Builder()
        builder.files?.forEach {
            val f = it.file
            val rq = RequestBody.create(MediaType.parse(builder.contentType), f)
            val progressBody = ProgressRequestBody(rq, it, this)
            body.addFormDataPart(it.paramName, it.name, progressBody)
        } ?: return
        builder.params.forEach { (s, s1) ->
            body.addFormDataPart(s, s1 ?: "")
        }
        reqCompo = api.upload(url, body.build()).call(builder.lo, Schedulers.io(), subscribeOn) { isSuccess, data, throwable, a ->
            if (isSuccess) {
                observer?.onSuccess(builder.callId, data, totalBytes)
            } else {
                observer?.onError(builder.callId, null, throwable, a)
            }
            observer?.onCompleted(builder.callId, null, totalBytes)
            destroy()
        }
    }

    override fun onComplete(info: FileInfo, contentLength: Long) {
        if (builder.deleteCompressFile) delete(info.path)
        observer?.onUploaded(builder.callId, info, contentLength)
    }
}