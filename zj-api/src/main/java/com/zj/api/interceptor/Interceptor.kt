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


class Interceptor(private val header: MutableMap<String, String>? = null, private val urlProvider: UrlProvider?) : Interceptor {

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
        header?.let {
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
                val pl = header?.get("Content-Type") ?: "application/json"
                val body = chain.request().body()!!
                if (body is FormBody) {
                    val rootMap = HashMap<String, Any>()
                    for (i in 0 until body.size()) {
                        rootMap[body.encodedName(i)] = URLDecoder.decode(body.encodedValue(i), "utf-8")
                    }
                    chain.proceed((newBuilder.post(Gson().toJson(rootMap).toRequestBody(MediaType.get(pl))).build()))
                } else {
                    chain.proceed(newBuilder.post(body).build())
                }
            } else {
                chain.proceed(newBuilder.build())
            }
        } catch (e: IllegalArgumentException) {
            onErrorResponse(request, 500, "connection failed")
        } catch (e: java.lang.IllegalArgumentException) {
            onErrorResponse(request, 500, "connection failed")
        } catch (e: IOException) {
            onErrorResponse(request, 404, "IO exception")
        } catch (e: Exception) {
            onErrorResponse(request, 400, "Unknown error")
        } catch (e: java.lang.Exception) {
            onErrorResponse(request, 400, "Unknown error")
        }
    }

    private fun onErrorResponse(request: Request, code: Int, message: String): Response {
        return Response.Builder().code(code).message(message).body(ResponseBody.create(MediaType.get("application/json"), "UNKNOWN ERROR")).request(request).protocol(Protocol.HTTP_2).build()
    }
}