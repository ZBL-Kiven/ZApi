package com.zj.api.base

import com.zj.api.adapt.AdapterPendingData
import com.zj.api.eh.ErrorHandler
import io.reactivex.Observable

internal abstract class BaseErrorHandlerObservable<T, R>(protected val pendingData: AdapterPendingData<R>) : Observable<T>()