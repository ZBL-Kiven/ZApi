package com.zj.api.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.zj.api.ZApi
import com.zj.api.adapt.AdapterPendingData
import com.zj.api.eh.EHParam
import com.zj.api.eh.ErrorHandler
import com.zj.api.eh.LimitScope
import com.zj.api.exception.ApiException
import com.zj.api.interfaces.ResponseHandler
import com.zj.ok3.Call
import com.zj.ok3.HttpException
import com.zj.ok3.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.lang.reflect.*
import java.lang.reflect.Array
import java.util.*
import java.util.concurrent.TimeoutException

internal object Constance {

    fun <T> checkMockedValid(pendingData: AdapterPendingData<T>, returnType: Type) {
        if (pendingData.mockData == null || returnType == Any::class.java) return
        val cls = pendingData.mockData.javaClass
        val mockType = cls.getDeclaredMethod("getMockData", EHParam::class.java).returnType

        // Some types need to be integrated, such as the interoperable types that exist between Java and Kotlin
        if (getRawType(returnType) != getRawType(mockType)) {
            throw IllegalArgumentException("the mockAble<$returnType> annotation has present, but the mocked class return type is <$mockType>")
        }
    }

    fun <T, R> parseBodyResponse(pendingData: AdapterPendingData<T>, response: Response<T?>, handler: R, rh: ResponseHandler<T?, R>) {
        val body = response.body()
        val code = response.code()
        if (body != null) {
            dealSuccessDataWithEh(pendingData, code, body) {
                rh.onSuccess(code, it, handler)
            }
        }
        if (response.errorBody() != null) {
            val httpException = HttpException(response)
            dealErrorWithEH(pendingData, code, response, httpException) { e, a ->
                rh.onError(e, a, handler)
            }
        }
    }

    fun <T, R> dealSuccessDataWithEh(pendingData: AdapterPendingData<R>, code: Int, body: T?, done: (T?) -> Unit) {
        if (pendingData.errorHandler == null) {
            done(body)
            return
        }
        var result: T? = null
        val job = runWithScope(pendingData.handleScheduler.successScope).launch {
            result = pendingData.errorHandler.interruptSuccessBody(pendingData.handleScheduler.id, code, body, pendingData.methodParamData)
        }
        while (result == null && !pendingData.isTimeOut) {
            pendingData.perWait(100)
            Thread.sleep(100)
        }
        result?.let {
            done(it)
        } ?: run {
            job.cancel()
            done(null)
        }
    }

    private fun <T, R> dealErrorWithEH(pendingData: AdapterPendingData<R>, code: Int, response: Response<T>, e: Throwable, done: (ApiException, Any?) -> Unit) {
        val url = response.raw().request().url().toString()
        val headers = mutableMapOf<String, String>()
        response.raw().request().headers().toMultimap().forEach { (t, u) ->
            headers[t] = u.joinToString { it }
        }
        val eh = pendingData.errorHandler
        val he = parseOrCreateHttpExceptionByCall(pendingData.handleScheduler.id, e, code, url, headers)
        if (eh == null) {
            done(he, null)
            return
        }
        dealErrorWithHandler(pendingData, eh, he, done)
    }

    fun <T, R> dealErrorWithEH(pendingData: AdapterPendingData<R>, code: Int, call: Call<T>, e: Throwable, done: (ApiException, Any?) -> Unit) {
        val url = call.request().url().toString()
        val headers = mutableMapOf<String, String>()
        call.request().headers().toMultimap().forEach { (t, u) ->
            headers[t] = u.joinToString { it }
        }
        val eh = pendingData.errorHandler
        val he = parseOrCreateHttpExceptionByCall(pendingData.handleScheduler.id, e, code, url, headers)
        if (eh == null) {
            done(he, null)
            return
        }
        dealErrorWithHandler(pendingData, eh, he, done)
    }

    private fun <R> dealErrorWithHandler(pendingData: AdapterPendingData<R>, eh: ErrorHandler, he: ApiException, done: (ApiException, Any?) -> Unit) {
        var result: Pair<Boolean, Any?>? = null
        val job = runWithScope(pendingData.handleScheduler.errorScope).launch {
            result = eh.interruptErrorBody(he, pendingData.methodParamData)
        }
        while (result == null && !pendingData.isTimeOut) {
            pendingData.perWait(100)
            Thread.sleep(100)
        }
        result?.let {
            pendingData.resetTimeOut()
            if (!it.first) {
                done(he, it.second)
            }
        } ?: run {
            job.cancel()
            done(he, TimeoutException("The error data deal in ErrorHandler has been timeout after ${pendingData.handleScheduler.timeOut}-mills ,check of your [ErrorHandler.interruptErrorBody] or present @ApiHandler annotation on service method to set longer EH.timeout"))
        }
    }

    private fun runWithScope(scope: String): CoroutineScope {
        return when (scope) {
            ZApi.IO -> CoroutineScope(Dispatchers.IO)
            ZApi.CALCULATE -> CoroutineScope(Dispatchers.Default)
            else -> MainScope()
        }
    }

    private fun parseOrCreateHttpExceptionByCall(id: String, e: Throwable? = null, code: Int = 400, url: String?, headers: Map<String, String>?): ApiException {
        return when (e) {
            is HttpException -> {
                return ApiException(id, e, e)
            }
            is ApiException -> e
            else -> {
                parseOrCreateHttpException(id, url, headers, e, code)
            }
        }
    }

    fun parseOrCreateHttpException(id: String, url: String?, header: Map<String, String?>?, throwable: Throwable?, codeDefault: Int = 400): ApiException {
        val httpException = if (throwable is HttpException) throwable else {
            val sb = StringBuilder().append("{").append("\"message\":\"parsed unknown error with : ").append(throwable?.message).append("\"")
            val responseBody = ResponseBody.create(MediaType.get("Application/json"), sb.toString())
            val raw = okhttp3.Response.Builder().body(responseBody).code(codeDefault).message(sb.toString()).protocol(Protocol.HTTP_1_1).request(Request.Builder().url(url ?: "https://unkown-host").headers(Headers.of(header ?: mapOf())).build()).build()
            HttpException(Response.error<ResponseBody>(responseBody, raw))
        }
        return ApiException(id, httpException, throwable)
    }

    fun <T, R> withScheduler(t: T, @LimitScope scheduler: String, lo: LifecycleOwner?, od: suspend T.() -> R?) {
        val s = when (scheduler) {
            ZApi.CALCULATE -> Dispatchers.Default
            ZApi.IO -> Dispatchers.IO
            else -> Dispatchers.Main
        }
        val scope = lo?.lifecycle?.coroutineScope ?: CoroutineScope(s)
        scope.launch {
            od.invoke(t)
        }
    }

    fun getRawType(type: Type): Class<*> {
        Objects.requireNonNull(type, "type == null")
        if (type is Class<*>) { // Type is a normal class.
            return type
        }
        if (type is ParameterizedType) {

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            val rawType = type.rawType
            require(rawType is Class<*>)
            return rawType
        }
        if (type is GenericArrayType) {
            val componentType = type.genericComponentType
            return Array.newInstance(getRawType(componentType), 0).javaClass
        }
        if (type is TypeVariable<*>) {

            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Any::class.java
        }
        if (type is WildcardType) {
            return getRawType(type.upperBounds[0])
        }
        throw IllegalArgumentException("Expected a Class, ParameterizedType, or " + "GenericArrayType, but <" + type + "> is of type " + type.javaClass.name)
    }
}