package com.zj.api.uploader.task

import com.zj.api.ZApi
import com.zj.api.call
import com.zj.api.interceptor.HeaderProvider
import com.zj.api.interceptor.UrlProvider
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.progress.ProgressListener
import com.zj.api.progress.ProgressRequestBody
import com.zj.api.uploader.FileUploadListener
import com.zj.api.uploader.UploadBuilder
import com.zj.api.uploader.ZUploadService
import com.zj.api.utils.Constance
import com.zj.api.utils.LogUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@Suppress("SpellCheckingInspection", "unused")
class UploadTask internal constructor(private val builder: UploadBuilder, private val observer: FileUploadListener) : ProgressListener {

    private var reqCompo: RequestCancelable? = null
    private var totalBytes: Long = 0

    init {
        upload()
    }

    private fun upload() {
        val part = builder.fileInfo?.let {
            val f = File(it.path)
            val rq = RequestBody.create(MediaType.parse(builder.contentType), f)
            val progressBody = ProgressRequestBody(rq, it.uploadIndex, this)
            MultipartBody.Part.createFormData(it.paramName, it.name, progressBody)
        } ?: return
        val map = mutableMapOf<String, RequestBody>()
        builder.params?.forEach { (s, s1) ->
            val paramsPart = RequestBody.create(MediaType.parse(builder.contentType), s1 ?: "")
            map[s] = paramsPart
        }
        val url = object : UrlProvider() {
            override fun url(): String {
                return builder.url.toString()
            }
        }

        val header = object : HeaderProvider {
            override fun headers(): Map<out String, String> {
                return builder.headers ?: mutableMapOf()
            }
        }
        val subscribeOn = when (builder.scheduler) {
            ZApi.CALCULATE -> Schedulers.computation()
            ZApi.IO -> Schedulers.io()
            else -> AndroidSchedulers.mainThread()
        }
        val api = ZApi.create(ZUploadService::class.java, builder.errorHandler).header(header).baseUrl(url).build()
        reqCompo = api.upload(map, part).call(builder.lo, Schedulers.io(), subscribeOn) { isSuccess, data, throwable, a ->
            if (isSuccess) {
                if (builder.deleteCompressFile) delete(builder.fileInfo?.path)
                observer.onSuccess(builder.callId, data, totalBytes)
            } else {
                observer.onError(builder.callId, throwable, a)
            }
            observer.onCompleted(builder.callId)
        }
    }

    fun cancel() {
        observer.onError(builder.callId, Constance.parseOrCreateHttpException(builder.callId, builder.url.toString(), builder.params, InterruptedException("canceled!!")), null)
        reqCompo?.cancel()
    }

    fun destroy() {
        builder.invalid()
        reqCompo?.cancel()
    }

    override fun onProgress(fileIndex: Int, progress: Int, contentLength: Long) {
        this.totalBytes = contentLength
        observer.onProgress(builder.callId, progress)
    }

    private fun delete(path: String?) {
        if (path.isNullOrEmpty()) return
        val file = File(path)
        if (file.exists() && file.delete()) {
            LogUtils.d("FileUploadTask", "the temp file $path has delete success!")
        } else {
            LogUtils.d("FileUploadTask", "the temp file $path was delete failed!")
        }
    }
}