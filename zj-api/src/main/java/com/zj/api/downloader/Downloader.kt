package com.zj.api.downloader

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
internal object Downloader {

    fun writeResponseToDisk(builder: DownloadBuilder, response: Response<ResponseBody>) {
        val body = response.body()
        if (body == null) {
            builder.result { onError(NullPointerException("the downloaded response body was null.")) }
            return
        }
        writeFileFromIS(builder, body.byteStream(), body.contentLength())
    }

    fun writeFileFromIS(builder: DownloadBuilder, `is`: InputStream, totalLength: Long) {
        var file: File? = builder.target
        builder.result { onStart() }
        file = createFile(file)
        if (file == null) {
            builder.result { onError(IOException("create new file :IOException")) }
            return
        }
        var os: OutputStream? = null
        var currentLength: Long = 0
        try {
            os = FileOutputStream(file)
            val sBufferSize = 8192
            val data = ByteArray(sBufferSize)
            var len: Int
            var last = -1
            while (`is`.read(data, 0, sBufferSize).also { len = it } != -1) {
                os.write(data, 0, len)
                currentLength += len.toLong()
                val curProgress = (100f * currentLength / totalLength).toInt()
                if (last < curProgress) builder.result { onProgress(curProgress) }
                last = curProgress
            }
            builder.result { onCompleted(file.absolutePath) }
        } catch (e: IOException) {
            builder.result { onError(IOException("cannot to write bytes to file ,case :" + e.message)) }
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun DownloadListener.result() {

    }

    private fun createFile(source: File?): File? {
        val parent = source?.parentFile ?: return null
        if (!parent.exists()) {
            if (!parent.mkdirs()) return null
        }
        try {
            if (!source.exists() && !source.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            return null
        }
        return source
    }
}