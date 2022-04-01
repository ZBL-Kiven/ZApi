package com.zj.api.downloader

interface DownloadListener {

    suspend fun onStart() {}

    suspend fun onCompleted(absolutePath: String) {}

    suspend fun onProgress(progress: Int) {}

    suspend fun onError(e: Throwable?, isCanceled: Boolean = false) {}
}

