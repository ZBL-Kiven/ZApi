package com.zj.api.uploader


import io.reactivex.Observable
import io.reactivex.Observer

class ProgressObservable<T>(private val upstream: Observable<in T>?) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>?) {
        upstream?.subscribe()
    }
}