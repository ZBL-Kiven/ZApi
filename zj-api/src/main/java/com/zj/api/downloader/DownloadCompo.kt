package com.zj.api.downloader

import com.zj.api.downloader.Downloader.writeResponseToDisk
import com.zj.api.interfaces.RequestCancelable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.util.concurrent.Executors


class DownloadCompo internal constructor(target: File, url: String, private val listener: DownloadListener) : RequestCancelable {

    private val compo: Call<ResponseBody>?

    interface DownloadService {

        @Streaming
        @GET
        fun download(@Url url: String): Call<ResponseBody>?
    }

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://xxx.com").callbackExecutor(Executors.newSingleThreadExecutor()).build()
        compo = retrofit.create(DownloadService::class.java).download(url)
        compo?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                writeResponseToDisk(target.path, response, listener)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener.onError(t)
            }
        })
    }

    override fun cancel(msg: String?, throwable: Throwable?) {
        compo?.cancel()
        if (msg.isNullOrEmpty().not() || throwable != null) listener.onError(Exception(msg, throwable), true)
    }
}