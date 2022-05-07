package com.zj.api.eh

import com.zj.api.exception.ApiException


/**
 * Set it when you create your service, eg: [com.zj.api.ZApi.create],it includes the following functions.
 *
 * 1.Allows you to intercept,change and other operations on successfully executed data, see [interruptSuccessBody]
 *
 * 2.allows you to process all errors (internal, external, self-throw) of the executed function in advance,
 * before completing the function execution. see [interruptErrorBody]
 * */
abstract class ErrorHandler {

    /**
     * Thread : Called on the main thread by default, you can define a separate thread for each execution function through the @[ApiHandler.errorEHScope] annotation.
     *
     * @return First = true , This error will be intercepted (consumed),
     * at which point the error will no longer be called back to the execution function as a result.
     *
     * @return Second This value will be called back to the function execution result,
     * such as [com.zj.api.call.coroutine.SuspendObservable.fromErrorHandler], where fromErrorHandler is the return value,
     * which is often used at the calling place after handling special errors.
     * */
    open suspend fun interruptErrorBody(throwable: ApiException?, ehParams: EHParam): Pair<Boolean, Any?> {
        return Pair(false, null)
    }


    /**
     * It is allowed to modify the return value here,
     * but it is not recommended to be used as Mock . Mock related support can be annotated with [com.zj.api.mock.Mock].
     * Override this method to perform data retrieval,
     * interception, modification, etc. if it is considered successful, such as HttpCode = 204.
     * */
    open suspend fun <R> interruptSuccessBody(id: String, code: Int, data: R?, ehParams: EHParam): R? {
        return data
    }
}
