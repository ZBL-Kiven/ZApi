package com.zj.api.base

import com.zj.api.adapt.AdapterPendingData
import io.reactivex.Observable

internal abstract class BaseErrorHandlerObservable<T, R>(protected val pendingData: AdapterPendingData<R>) : Observable<T>()