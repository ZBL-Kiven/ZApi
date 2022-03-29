package com.zj.api.uploader

import com.zj.api.exception.ApiException

interface FileUploadListener {

    fun onCompleted(uploadId: String)

    fun onError(uploadId: String, exception: ApiException?, errorBody: Any?)

    fun onProgress(uploadId: String, progress: Int)

    fun onSuccess(uploadId: String, body: Any?, totalBytes: Long)
}
