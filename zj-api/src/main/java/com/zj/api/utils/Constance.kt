package com.zj.api.utils

import com.zj.api.adapt.HandledException
import com.zj.api.exception.ApiException
import com.zj.api.interfaces.ErrorHandler
import io.reactivex.Observer
import okhttp3.*
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

internal object Constance {

    const val HTTPS = "https"

    private fun parseOrCreateHttpExceptionByCall(e: Throwable? = null, code: Int = 400, call: Call<*>): ApiException {
        return if (e is ApiException) e else {
            val url = call.request().url().toString()
            val headers = mutableMapOf<String, String>()
            call.request().headers().toMultimap().forEach { (t, u) ->
                headers[t] = u.joinToString { it }
            }
            parseOrCreateHttpException(url, headers, e, code)
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

    fun <R> dealSuccessDataWithEh(errorHandler: ErrorHandler?, body: R?, done: (R?) -> Unit) {
        if (errorHandler == null) {
            done(body)
            return
        }
        done(errorHandler.interruptSuccessBody(body))
    }

    fun <R, O> dealExceptionWithEhForObservers(errorHandler: ErrorHandler?, t: Throwable, code: Int, call: Call<R?>, observer: Observer<in O?>) {
        if (errorHandler == null) {
            observer.onError(t)
            return
        }
        val e = parseOrCreateHttpExceptionByCall(t, code, call)
        val p = errorHandler.interruptErrorBody(e)
        if (!p.first || p.second != null) {
            observer.onError(HandledException(e, p.second))
        }
    }

    fun <F> dealErrorWithEH(eh: ErrorHandler?, code: Int, call: Call<F?>, e: Throwable, done: (ApiException, Any?) -> Unit) {
        val he = parseOrCreateHttpExceptionByCall(e, code, call)
        if (eh == null) {
            done(he, null)
            return
        }
        val p = eh.interruptErrorBody(he)
        if (!p.first || p.second != null) {
            done(he, p.second)
        }
    }
}