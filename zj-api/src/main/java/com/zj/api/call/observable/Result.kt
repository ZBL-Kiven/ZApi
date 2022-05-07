@file:Suppress("unused")

package com.zj.api.call.observable

import com.zj.ok3.Response

/** The result of executing an HTTP request.  */
internal class Result<T> private constructor(private val response: Response<T>?, private val error: Throwable?) {

    /** `true` if the request resulted in an error. See [.error] for the cause.  */
    val isError: Boolean
        get() = error != null

    /**
     * The response received from executing an HTTP request. Only present when [.isError] is
     * false, null otherwise.
     */
    fun response(): Response<T>? {
        return response
    }

    fun error(): Throwable? {
        return error
    }

    companion object {
        @JvmStatic
        fun <T> error(error: Throwable?): Result<T> {
            if (error == null) throw NullPointerException("error == null")
            return Result(null, error)
        }

        @JvmStatic
        fun <T> response(response: Response<T>?): Result<T> {
            if (response == null) throw NullPointerException("response == null")
            return Result(response, null)
        }
    }
}
