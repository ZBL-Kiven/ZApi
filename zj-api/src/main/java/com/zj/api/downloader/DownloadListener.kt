package com.zj.api.downloader

interface DownloadListener {

    fun onStart() {}

    fun onCompleted(absolutePath: String) {}

    fun onProgress(progress: Int) {}

    fun onError(e: Throwable?, isCanceled: Boolean = false) {}
}