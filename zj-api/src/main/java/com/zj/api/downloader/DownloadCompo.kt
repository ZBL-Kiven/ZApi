package com.zj.api.downloader

import com.zj.api.ZApi
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.utils.LogUtils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class DownloadCompo internal constructor(private val callId: String, private val builder: DownloadBuilder) : RequestCancelable {

    private var disposable: Disposable? = null

    private val call = ZApi.create(DownloadService::class.java, builder.errorHandler) //
        .header(builder.headers) //
        .timeOut(builder.timeout) //
        .debugAble(false) //
        .build() //
        .download(builder.url.url())

    init {
        call.doOnNext {
            Downloader.writeResponseToDisk(builder, it)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<ResponseBody> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onNext(t: ResponseBody) {
                LogUtils.d("DownloadCompo", "A task [$callId] has been downloaded!")
            }

            override fun onError(e: Throwable) {
                builder.result { onError(callId, e) }
            }

            override fun onComplete() {
                builder.result { onCompleted() }
            }
        })
    }

    override fun cancel(msg: String?, throwable: Throwable?) {
        disposable?.dispose()
        if (msg.isNullOrEmpty().not() || throwable != null) {
            builder.result { onError(callId, Exception(msg, throwable), true) }
        }
    }
}