package com.zj.api.downloader

import okhttp3.ResponseBody
import retrofit2.Call

interface DownloadInterceptor {

    fun intercept(builder: DownloadBody): Call<ResponseBody>

}
