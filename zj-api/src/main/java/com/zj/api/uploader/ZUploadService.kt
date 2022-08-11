package com.zj.api.uploader

import com.zj.api.eh.ApiHandler
import com.zj.ok3.http.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ZUploadService {

    @Multipart
    @POST
    @ApiHandler(writeTimeOut = Int.MAX_VALUE)
    fun upload(@Url url: String, @PartMap params: @JvmSuppressWildcards Map<String, RequestBody>, @Part file: MultipartBody.Part): Observable<Any?>

    @POST
    @ApiHandler(writeTimeOut = Int.MAX_VALUE)
    fun upload(@Url url: String, @Body body: MultipartBody): Observable<Any?>
}