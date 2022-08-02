@file:Suppress("unused")

package com.zj.api.interceptor

import com.zj.api.utils.LogUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

class HttpLoggingInterceptor constructor(private val clsName: String) : Interceptor {

    @Volatile private var headersToRedact = emptySet<String>()

    private val logger = { message: String ->
        LogUtils.d(clsName, message)
    }

    @Volatile private var level: Int = LogLevel.NONE.with

    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact.addAll(headersToRedact)
        newHeadersToRedact.add(name)
        headersToRedact = newHeadersToRedact
    }

    /** Change the level at which this interceptor logs.  */
    fun setLevel(level: Int?): HttpLoggingInterceptor {
        if (level == null) throw NullPointerException("level == null. Use Level.NONE instead.")
        this.level = level
        return this
    }

    private fun getLevel(): Int {
        return level
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (getLevel() == 0) {
            return chain.proceed(request)
        }
        val logBasic = getLevel().and(LogLevel.BASIC.with) != 0
        val logBody = getLevel().and(LogLevel.REQUEST_BODY.with) != 0
        val logResultBody = getLevel().and(LogLevel.RESULT_BODY.with) != 0
        val logHeaders = getLevel().and(LogLevel.HEADERS.with) != 0
        val requestBody = request.body()
        val hasRequestBody = requestBody != null
        val connection = chain.connection()

        //start
        if (logBasic) {
            logger.invoke(" ")
            var requestStartMessage = ("===> " + request.method() + ' '.toString() + request.url() + if (connection != null) " " + connection.protocol() else "")
            if (!logHeaders && hasRequestBody) {
                requestStartMessage += " (" + requestBody?.contentLength() + "-byte body)"
            }
            logger.invoke(requestStartMessage)
        }

        if (logHeaders) {
            logger.invoke(" ")
            logger.invoke("------------------------------ headers start ----------------------------")
            if (hasRequestBody) { // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody?.contentType() != null) {
                    logger.invoke("Content-Type: " + requestBody.contentType())
                }
                if (requestBody?.contentLength() != -1L) {
                    logger.invoke("Content-Length: " + requestBody?.contentLength())
                }
            }
            val headers = request.headers()
            var i = 0
            val count = headers.size()
            while (i < count) {
                val name = headers.name(i) // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                    logHeader(headers, i)
                }
                i++
            }
        }
        val contentLen = requestBody?.contentLength()
        if (!logBody || !hasRequestBody) {
            logger.invoke("===> END " + request.method())
        } else if (bodyHasUnknownEncoding(request.headers())) {
            logger.invoke("===> END " + request.method() + " (encoded body omitted)")
        } else {
            logger.invoke(" ")
            val buffer = Buffer()
            requestBody?.writeTo(buffer)
            var charset = UTF8 ?: Charset.defaultCharset()
            val contentType = requestBody?.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            if (isPlaintext(buffer)) {
                if (logBody) {
                    logger.invoke("------------------------------ body start ----------------------------")
                    logger.invoke(buffer.readString(charset))
                }
                logger.invoke("===> END " + request.method() + " (" + contentLen + "-byte body)")
            } else {
                logger.invoke("===> END " + request.method() + " (binary " + requestBody?.contentLength() + "-byte body omitted)")
            }
        }
        LogUtils.onSizeParsed(clsName, true, contentLen ?: 0L)
        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.invoke(" ")
            logger.invoke("<=== HTTP FAILED: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body() ?: return response
        val contentLength = responseBody.contentLength()

        if (logBasic) {
            val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
            logger.invoke(" ")
            logger.invoke("------------------------------ result ----------------------------")
            logger.invoke("<=== " + response.code() + (if (response.message().isEmpty()) "" else ' ' + response.message()) + ' '.toString() + response.request().url() + " (" + tookMs + "ms" + (if (!logHeaders) ", $bodySize body" else "") + ')'.toString())
        }
        val headers = response.headers()
        if (getLevel().and(LogLevel.SERVER_HEADERS.with) != 0) {
            logger.invoke("------------------------------ server headers start----------------------------")
            var i = 0
            val count = headers.size()
            while (i < count) {
                logHeader(headers, i)
                i++
            }
        }
        if (!HttpHeaders.hasBody(response)) {
            logger.invoke("<=== END HTTP")
        } else if (bodyHasUnknownEncoding(response.headers())) {
            logger.invoke("<=== END HTTP (encoded body omitted)")
        } else {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer
            var gzippedLength: Long? = null
            if ("gzip".equals(headers.get("Content-Encoding") ?: "", ignoreCase = true)) {
                gzippedLength = buffer.size()
                val buf = buffer.clone()
                GzipSource(buf).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            var charset = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8) ?: Charset.defaultCharset()
            }
            logger.invoke("------------------------------ response body start ----------------------------")
            if (!isPlaintext(buffer)) {
                logger.invoke("<=== END HTTP (binary " + buffer.size() + "-byte body omitted)")
                return response
            }
            if (logResultBody && contentLength != 0L) {
                logger.invoke(buffer.clone().readString(charset))
            }
            val length = buffer.size()
            if (gzippedLength != null) {
                LogUtils.onSizeParsed(clsName, false, gzippedLength)
                logger.invoke("<=== END HTTP ($length-byte, $gzippedLength-gzipped-byte body)")
            } else {
                LogUtils.onSizeParsed(clsName, false, length)
                logger.invoke("<=== END HTTP ($length-byte body)")
            }
            logger.invoke(" ")
        }

        return response
    }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headersToRedact.contains(headers.name(i))) " " else headers.value(i)
        logger.invoke(headers.name(i) + ": " + value)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }

        }

        private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
            val contentEncoding = headers.get("Content-Encoding")
            return (contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true) && !contentEncoding.equals("gzip", ignoreCase = true))
        }
    }
}

enum class LogLevel(private val origin: Int, internal var with: Int = origin) {

    NONE(0), SERVER_HEADERS(1 shl 2), HEADERS(1 shl 3), REQUEST_BODY(1 shl 4), BASIC(1 shl 5), RESULT_BODY(1 shl 6);

    fun pollWith(): Int {
        val with = this.with
        this.with = this.origin
        return with
    }

    companion object {
        val Internal: LogLevel; get() = BASIC + HEADERS + REQUEST_BODY + SERVER_HEADERS + RESULT_BODY
    }
}

operator fun LogLevel.plus(other: LogLevel): LogLevel {
    return apply { with = with.or(other.with) }
}