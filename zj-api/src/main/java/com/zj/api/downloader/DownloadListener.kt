package com.zj.api.downloader


interface DownloadListener {

    suspend fun onStart(callId: String) {}

    suspend fun onCompleted(callId: String, absolutePath: String) {}

    suspend fun onProgress(callId: String, progress: Int) {}

    suspend fun onError(callId: String, e: Throwable?, isCanceled: Boolean = false) {}
}

