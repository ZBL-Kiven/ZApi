package com.zj.api.exception

import com.zj.ok3.HttpException
import java.net.*


/**
 * @param id is generated by default,customized by using the [com.zj.api.eh.ApiHandler] annotation，@see [com.zj.api.eh.ApiHandler.id] to learn how to use.
 *
 * @param httpException Exception for an unexpected, non-2xx HTTP response.
 *
 * @param case It always coexists with [httpException], some errors are non-HttpException,
 * I have generated a unified HttpException based on it,
 * but for better extensibility (such as implementing a custom CallAdapterFactory and need to throw an exception for easy identification when using case),
 * it will work better than HttpException.
 * */
@Suppress("unused")
class ApiException(val id: String, val httpException: HttpException?, case: Throwable?) : Throwable(case) {

    companion object {
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val REQUEST_TIMEOUT = 408
        const val INTERNAL_SERVER_ERROR = 500
        const val BAD_GATEWAY = 502
        const val SERVICE_UNAVAILABLE = 503
        const val GATEWAY_TIMEOUT = 504
    }

    fun isHttpException(): Boolean {
        return when (httpException?.code()) {
            UNAUTHORIZED,
            FORBIDDEN,
            NOT_FOUND,
            REQUEST_TIMEOUT,
            INTERNAL_SERVER_ERROR,
            BAD_GATEWAY,
            SERVICE_UNAVAILABLE,
            GATEWAY_TIMEOUT,
            -> {
                true
            }
            else -> false
        }
    }

    fun isNetworkException(): Boolean {
        return when (cause) {
            is UnknownHostException,
            is SocketTimeoutException,
            is SocketException,
            is ProtocolException,
            is PortUnreachableException,
            is NoRouteToHostException,
            is ConnectException,
            -> {
                true
            }
            else -> false
        }
    }
}