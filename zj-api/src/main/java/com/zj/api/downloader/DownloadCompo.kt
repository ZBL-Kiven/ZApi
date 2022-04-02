package com.zj.api.downloader

import com.zj.api.ZApi
import com.zj.api.downloader.Downloader.writeResponseToDisk
import com.zj.api.interfaces.RequestCancelable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

class DownloadCompo internal constructor(val callId: String, private val builder: DownloadBuilder) : RequestCancelable {

    interface DownloadService {

        @Streaming
        @GET
        fun download(@Url url: String): Call<ResponseBody>
    }

    private val call = builder.downloadInterceptor?.intercept(builder as DownloadBody)  //
        ?: ZApi.create(DownloadService::class.java, builder.errorHandler) //
            .header(builder.headers) //
            .timeOut(builder.timeout) //
            .build() //
            .download(builder.url.url()) //

    init {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                writeResponseToDisk(builder, response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                builder.result { onError(callId, t) }
            }
        })
    }

    override fun cancel(msg: String?, throwable: Throwable?) {
        call.cancel()
        if (msg.isNullOrEmpty().not() || throwable != null) {
            builder.result { onError(callId, Exception(msg, throwable), true) }
        }
    }
}