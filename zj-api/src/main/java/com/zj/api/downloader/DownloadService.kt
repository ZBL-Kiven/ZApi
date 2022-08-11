package com.zj.api.downloader

import com.zj.api.eh.ApiHandler
import com.zj.ok3.http.GET
import com.zj.ok3.http.Streaming
import com.zj.ok3.http.Url
import io.reactivex.Observable
import okhttp3.ResponseBody

internal interface DownloadService {
    @Streaming
    @GET
    @ApiHandler(timeOut = 10000)
    fun download(@Url url: String): Observable<ResponseBody>
}