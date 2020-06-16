package com.zj.api.base

//package com.zbl.api
//
//import com.zbl.api.interceptor.HeaderProvider
//import com.zbl.api.interceptor.UrlProvider
//import io.reactivex.Observable
//import io.reactivex.Scheduler
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.functions.Function3
//import io.reactivex.schedulers.Schedulers
//import java.io.InputStream
//
//internal object ZipSubscribe {
//
//    fun <T, F, F2, F3, R> triple(service: T, observer1: (T) -> Observable<F>, observer2: (T) -> Observable<F2>, observer3: (T) -> Observable<F3>, onFunc: (F?, F2?, F3?) -> R, subscribe: ((isSuccess: Boolean, data1: R?, throwable: Throwable?) -> Unit)? = null, subSubscribeSchedulers: Scheduler = Schedulers.io(), observableSchedulers: Scheduler = AndroidSchedulers.mainThread()) {
//
//        val obs1 = observer1.invoke(service)
//        val obs2 = observer2.invoke(service)
//        val obs3 = observer3.invoke(service)
//        var data1: F? = null
//        var data2: F2? = null
//        var data3: F3? = null
//        var compo1: BaseRetrofit.RequestInCompo<F>? = null
//        var compo2: BaseRetrofit.RequestInCompo<F2>? = null
//        var compo3: BaseRetrofit.RequestInCompo<F3>? = null
//        val onError: (Throwable?) -> Unit = {
//            try {
//                compo1?.cancel()
//                compo2?.cancel()
//                compo3?.cancel()
//            } catch (e: Exception) {
//                it?.stackTrace?.plus(e.stackTrace)
//            }
//            subscribe?.invoke(false, null, it)
//        }
//        val s = Observable.zip(obs1, obs2, obs3, Function3 { t1, t2, t3 ->
//            compo1 = BaseRetrofit.RequestInCompo(obs1, Schedulers.io(), subSubscribeSchedulers, {
//                data1 = it
//            }, onError)
//            compo2 = BaseRetrofit.RequestInCompo(obs2, Schedulers.io(), subSubscribeSchedulers, {
//                data2 = it
//            }, onError)
//            compo3 = BaseRetrofit.RequestInCompo(obs3, Schedulers.io(), subSubscribeSchedulers, {
//                data3 = it
//            }, onError)
//            return@Function3 onFunc(data1, data2, data3)
//        }).subscribeOn(subSubscribeSchedulers).observeOn(observableSchedulers).subscribe {
//            subscribe?.invoke(it != null, it, null)
//        }
//    }
//
//}
