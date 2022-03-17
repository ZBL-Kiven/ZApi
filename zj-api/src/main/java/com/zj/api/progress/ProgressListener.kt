package com.zj.api.progress

interface ProgressListener {

    fun onProgress(fileIndex: Int, progress: Int, contentLength: Long)
}