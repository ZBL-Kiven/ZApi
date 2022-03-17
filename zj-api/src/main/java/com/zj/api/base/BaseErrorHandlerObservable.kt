package com.zj.api.base

import com.zj.api.interfaces.ErrorHandler
import io.reactivex.Observable

abstract class BaseErrorHandlerObservable<T>(protected val errorHandler: ErrorHandler?) : Observable<T>()