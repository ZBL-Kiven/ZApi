package com.zj.api.uploader

import com.zj.ok3.http.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ZUploadService {

    @Multipart
    @POST
    fun upload(@Url url: String, @PartMap params: @JvmSuppressWildcards Map<String, RequestBody>, @Part file: MultipartBody.Part): Observable<Any?>

    @POST
    fun upload(@Url url: String, @Body body: MultipartBody): Observable<Any?>
}