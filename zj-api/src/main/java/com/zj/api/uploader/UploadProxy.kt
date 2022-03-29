package com.zj.api.uploader

import io.reactivex.Observable

class UploadProxy<T> internal constructor(private val observable: Observable<T>,internal val l: FileUploadListener) {





}