package com.zj.apiTest

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zj.api.ZApi
import com.zj.api.call
import com.zj.api.downloader.SimpleDownloadListener
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.utils.LoggerInterface
import kotlinx.coroutines.launch
import java.io.File

@Suppress("unused")
class MainActivity : AppCompatActivity() {

    private val testService: TestService
        get() {
            return ZApi.create(TestService::class.java, ApiErrorHandler).baseUrl(Constance.getBaseUrl()).header(Constance.getHeader()).build()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ZApi.addFlowsListener(TestService::class.java, object : LoggerInterface {
            override fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) {
                Log.e("------", "from :$fromCls , onSizeParsed in thread : ${Thread.currentThread().name}: isSend = $isSend , size = $size")
            }
        })
    }

    /**
     * 使用 ZApi 下载远程文件
     * */
    fun downloadTest(v: View) {
        v as TextView
        val f = File(externalCacheDir, "ZApi_download_test.png")
        val url = "https://github.com/ZBL-Kiven/BaseApi/tree/master/raw/title_screen.png"
        ZApi.download(f, url, object : SimpleDownloadListener() {
            override fun onCompleted(absolutePath: String) {
                runOnUiThread {
                    v.text = absolutePath
                }
            }

            override fun onError(e: Throwable?, isCanceled: Boolean) {
                runOnUiThread { v.text = e?.message }
                e?.printStackTrace()
            }
        })
    }


    fun requestByCoroutineSimpleTest(v: View) {
        v as TextView
        lifecycleScope.launch {
            val obj = testService.getIpCourSimple("zh-cn")
            Log.d("------", "$obj")
            v.text = "$obj"
        }
    }

    fun requestByCoroutineTest(v: View) {
        v as TextView
        lifecycleScope.launch {
            val s = testService.getIpCour("zh-cn")
            Log.d("------", "data = ${s?.data}  e = ${s?.error}   handled =  ${s?.fromErrorHandler} , thread = ${Thread.currentThread().name}")
            v.text = "$s"
        }
    }

    fun requestTestByObserver(v: View) {
        v as TextView
        testService.getIpMock("zh-cn").call(this) { isSuccess, data, throwable, handled ->
            val s = "$isSuccess :  ${data.toString()}   ${throwable?.message}"
            Log.d("------", s)
            v.text = s
        }
    }

    fun requestTestByCancelAbleObserver(v: View) {
        v as TextView
        var compo: RequestCancelable? = null
        compo = testService.getIp("zh-cn").call { isSuccess, data, throwable, handled ->
            val s = "$isSuccess :  ${data.toString()}   ${throwable?.message}"
            Log.d("------", s)
            v.text = s
            compo?.cancel()
        }
    }
}