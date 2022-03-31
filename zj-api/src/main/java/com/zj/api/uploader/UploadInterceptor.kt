package com.zj.api.uploader

import com.zj.api.uploader.task.UploadBody

interface UploadInterceptor {

    fun intercept(builder: UploadBody, observer: FileUploadListener?): Boolean {
        return false
    }

    companion object {
        fun getDefault(): UploadInterceptor {
            return object : UploadInterceptor {}
        }
    }
}