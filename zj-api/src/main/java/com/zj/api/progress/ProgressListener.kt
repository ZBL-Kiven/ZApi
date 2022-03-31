package com.zj.api.progress

import com.zj.api.uploader.FileInfo

interface ProgressListener {

    fun onProgress(info: FileInfo, progress: Int, contentLength: Long)

    fun onComplete(info: FileInfo, contentLength: Long)
}