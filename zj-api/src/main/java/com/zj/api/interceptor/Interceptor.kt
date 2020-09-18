package com.zj.api.interceptor

import com.google.gson.Gson
import okhttp3.*
import okhttp3.Interceptor
import okhttp3.internal.Util.checkOffsetAndCount
import okio.BufferedSink
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*


class Interceptor(private val headerProvider: HeaderProvider? = null, private val urlProvider: UrlProvider?) : Interceptor {

    companion object {

        /**
         * Returns a new request body that transmits this string. If [contentType] is non-null and lacks
         * a charset, this will use UTF-8.
         */
        @JvmStatic
        @JvmName("create")
        fun String.toRequestBody(contentType: MediaType? = null): RequestBody {
            var charset: Charset = Charsets.UTF_8
            var finalContentType: MediaType? = contentType
            if (contentType != null) {
                val resolvedCharset = contentType.charset()
                if (resolvedCharset == null) {
                    charset = Charsets.UTF_8
                    finalContentType = "$contentType; charset=utf-8".toMediaTypeOrNull()
                } else {
                    charset = resolvedCharset
                }
            }
            val bytes = toByteArray(charset)
            return bytes.toRequestBody(finalContentType, 0, bytes.size)
        }

        /** Returns a new request body that transmits this. */
        @JvmOverloads
        @JvmStatic
        @JvmName("create")
        fun ByteArray.toRequestBody(contentType: MediaType? = null, offset: Int = 0, byteCount: Int = size): RequestBody {
            checkOffsetAndCount(size.toLong(), offset.toLong(), byteCount.toLong())
            return object : RequestBody() {
                override fun contentType() = contentType

                override fun contentLength() = byteCount.toLong()

                override fun writeTo(sink: BufferedSink) {
                    sink.write(this@toRequestBody, offset, byteCount)
                }
            }
        }

        /** Returns a media type for this, or null if this is not a well-formed media type. */
        @JvmStatic
        @JvmName("parse")
        fun String.toMediaTypeOrNull(): MediaType? {
            return try {
                MediaType.get(this)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response? {
        val request = chain.request()
        val newBuilder = request.newBuilder()
        urlProvider?.url()?.let {
            val proxy: UrlProvider.UrlProxy = urlProvider.getProxy()
            newBuilder.url(request.url().newBuilder().scheme(proxy.protocol).host(proxy.host).port(proxy.port).build())
        }
        headerProvider?.headers()?.let {
            if (!it.containsKey("Content-Type")) {
                newBuilder.addHeader("Content-Type", "application/json")
            }
            if (!it.containsKey("charset")) {
                newBuilder.addHeader("charset", "utf-8")
            }
            it.forEach { e ->
                newBuilder.addHeader(e.key, e.value)
            }
        }
        return try {
            if ("POST" == chain.request().method()) {
                val rootMap = HashMap<String, Any>()
                val body = chain.request().body()
                if (body is FormBody) {
                    for (i in 0 until body.size()) {
                        rootMap[body.encodedName(i)] = URLDecoder.decode(body.encodedValue(i), "utf-8")
                    }
                }
                val pl = headerProvider?.headers()?.get("Content-Type") ?: "application/json"
                chain.proceed((newBuilder.post(Gson().toJson(rootMap).toRequestBody(MediaType.get(pl))).build()))
            } else {
                chain.proceed(newBuilder.build())
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            chain.proceed(request)
        } catch (e: IOException) {
            e.printStackTrace()
            chain.proceed(request)
        }
    }
}