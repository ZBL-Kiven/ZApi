package com.zj.api.okhttp3

interface ProgressListener {

    fun onProgress(fileIndex: Int, progress: Int, contentLength: Long)
}