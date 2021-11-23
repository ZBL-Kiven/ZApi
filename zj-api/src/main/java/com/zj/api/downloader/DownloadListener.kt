package com.zj.api.downloader

interface DownloadListener {

    fun onStart()

    fun onCompleted(absolutePath: String)

    fun onProgress(i: Int)

    fun onError(e: Throwable?, isCanceled: Boolean = false)
}

@Suppress("unused")
open class SimpleDownloadListener : DownloadListener {

    override fun onStart() {}

    override fun onCompleted(absolutePath: String) {}

    override fun onProgress(i: Int) {}

    override fun onError(e: Throwable?, isCanceled: Boolean) {}

}