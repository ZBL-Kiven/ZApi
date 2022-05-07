package com.zj.api.downloader

import okhttp3.ResponseBody
import com.zj.ok3.Call

interface DownloadInterceptor {

    fun intercept(builder: DownloadBody): Call<ResponseBody>

}
