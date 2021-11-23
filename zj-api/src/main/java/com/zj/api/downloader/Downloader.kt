package com.zj.api.downloader

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
internal object Downloader {

    fun writeResponseToDisk(path: String, response: Response<ResponseBody>, downloadListener: DownloadListener) {
        val body = response.body()
        if (body == null) {
            downloadListener.onError(NullPointerException("the downloaded response body was null."))
            return
        }
        writeFileFromIS(File(path), body.byteStream(), body.contentLength(), downloadListener)
    }

    fun writeFileFromIS(f: File, `is`: InputStream, totalLength: Long, downloadListener: DownloadListener) {
        var file: File? = f
        downloadListener.onStart()
        file = createFile(file)
        if (file == null) {
            downloadListener.onError(IOException("create new file :IOException"))
            return
        }
        var os: OutputStream? = null
        var currentLength: Long = 0
        try {
            os = FileOutputStream(file)
            val sBufferSize = 8192
            val data = ByteArray(sBufferSize)
            var len: Int
            while (`is`.read(data, 0, sBufferSize).also { len = it } != -1) {
                os.write(data, 0, len)
                currentLength += len.toLong()
                downloadListener.onProgress((100 * currentLength / totalLength).toInt())
            }
            downloadListener.onCompleted(file.absolutePath)
        } catch (e: IOException) {
            downloadListener.onError(IOException("cannot to write bytes to file ,case :" + e.message))
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