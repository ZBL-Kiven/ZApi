package com.zj.api.uploader

import com.zj.api.exception.ApiException

interface FileUploadListener {


    fun onCompleted(uploadId: String, fileInfo: FileInfo?, totalBytes: Long) {}

    fun onError(uploadId: String, fileInfo: FileInfo?, exception: ApiException?, errorBody: Any?){}

    fun onProgress(uploadId: String, fileInfo: FileInfo?, progress: Int, contentLength: Long) {}

    fun onSuccess(uploadId: String, body: Any?, totalBytes: Long){}

    fun onUploaded(uploadId: String, fileInfo: FileInfo?, contentLength: Long) {}
}
