package com.zj.api.uploader

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ZUploadService {

    @Multipart
    @POST
    fun upload(@PartMap params: @JvmSuppressWildcards Map<String, RequestBody>, @Part file: MultipartBody.Part): Observable<Any?>

    @Multipart
    @POST
    fun uploadMulti(@PartMap params: @JvmSuppressWildcards Map<String, RequestBody>, @Part file: MultipartBody.Part): Observable<Any?>
}