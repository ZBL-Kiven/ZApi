package com.zj.api.utils

import com.zj.api.exception.ApiException
import com.zj.api.interfaces.ErrorHandler
import com.zj.api.interfaces.ResponseHandler
import okhttp3.*
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

internal object Constance {

    const val HTTPS = "https"

    fun <T, R> parseBodyResponse(response: Response<T?>, handler: R, errorHandler: ErrorHandler?, rh: ResponseHandler<T?, R>) {
        val body = response.body()
        val code = response.code()
        if (body != null) {
            dealSuccessDataWithEh(errorHandler, code, body) {
                rh.onSuccess(code, it, handler)
            }
        }
        if (response.errorBody() != null) {
            val httpException = HttpException(response)
            dealErrorWithEH(errorHandler, code, response, httpException) { e, a ->
                rh.onError(e, a, handler)
            }
        }
    }

    fun <R> dealSuccessDataWithEh(errorHandler: ErrorHandler?, code: Int, body: R?, done: (R?) -> Unit) {
        if (errorHandler == null) {
            done(body)
            return
        }
        done(errorHandler.interruptSuccessBody(code, body))
    }

    private fun <T> dealErrorWithEH(eh: ErrorHandler?, code: Int, response: Response<T>, e: Throwable, done: (ApiException, Any?) -> Unit) {
        val url = response.raw().request().url().toString()
        val headers = mutableMapOf<String, String>()
        response.raw().request().headers().toMultimap().forEach { (t, u) ->
            headers[t] = u.joinToString { it }
        }
        val he = parseOrCreateHttpExceptionByCall(e, code, url, headers)
        if (eh == null) {
            done(he, null)
            return
        }
        val p = eh.interruptErrorBody(he)
        if (!p.first || p.second != null) {
            done(he, p.second)
        }
    }

    fun <T> dealErrorWithEH(eh: ErrorHandler?, code: Int, call: Call<T>, e: Throwable, done: (ApiException, Any?) -> Unit) {
        val url = call.request().url().toString()
        val headers = mutableMapOf<String, String>()
        call.request().headers().toMultimap().forEach { (t, u) ->
            headers[t] = u.joinToString { it }
        }
        val he = parseOrCreateHttpExceptionByCall(e, code, url, headers)
        if (eh == null) {
            done(he, null)
            return
        }
        val p = eh.interruptErrorBody(he)
        if (!p.first || p.second != null) {
            done(he, p.second)
        }
    }

    private fun parseOrCreateHttpExceptionByCall(e: Throwable? = null, code: Int = 400, url: String?, headers: Map<String, String>?): ApiException {
        return when (e) {
            is HttpException -> {
                return ApiException(e, e)
            }
            is ApiException -> e
            else -> {
                parseOrCreateHttpException(url, headers, e, code)
            }
        }
    }

    fun parseOrCreateHttpException(url: String?, header: Map<String, String>?, throwable: Throwable?, codeDefault: Int = 400): ApiException {
        val httpException = if (throwable is HttpException) throwable else {
            val sb = StringBuilder().append("{").append("\"message\":\"parsed unknown error with : ").append(throwable?.message).append("\"")
            val responseBody = ResponseBody.create(MediaType.get("Application/json"), sb.toString())
            val raw = okhttp3.Response.Builder().body(responseBody).code(codeDefault).message(sb.toString()).protocol(Protocol.HTTP_1_1).request(Request.Builder().url(url ?: "https://unkown-host").headers(Headers.of(header ?: mapOf())).build()).build()
            HttpException(Response.error<ResponseBody>(responseBody, raw))
        }
        return ApiException(httpException, throwable)
    }
}