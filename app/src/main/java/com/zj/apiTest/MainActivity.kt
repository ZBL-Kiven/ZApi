package com.zj.apiTest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zj.api.ZApi
import com.zj.api.call
import com.zj.api.downloader.SimpleDownloadListener
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.utils.LoggerInterface
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private val testService: TestService
        get() {
            return ZApi.create(TestService::class.java, ApiErrorHandler).baseUrl(Constance.getBaseUrl()).header(Constance.getHeader()).build()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ZApi.setLoggerInterface(TestService::class.java, object : LoggerInterface {
            override fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) {
                Log.e("------", "from :$fromCls , onSizeParsed in thread : ${Thread.currentThread().name}: isSend = $isSend , size = $size")
            }
        })

        tv?.setOnClickListener {
            requestByCoroutineTest()
        }
    }

    /**
     * 使用 ZApi 下载远程文件
     * */
    private fun downloadTest() {

        val f = File(externalCacheDir, "11221.aac")
        val url = "https://media.clipclaps.com/a/20211123/a/7/b/a7b04cef4dc74f8d9c9cb927b0752ba5.aac"
        ZApi.download(f, url, object : SimpleDownloadListener() {
            override fun onCompleted(absolutePath: String) {
                runOnUiThread {
                    tv?.text = absolutePath
                }
            }

            override fun onError(e: Throwable?, isCanceled: Boolean) {
                runOnUiThread { tv?.text = e?.message }
                e?.printStackTrace()
            }
        })
    }

    /**
     *
     *
     *
     * */
    private fun requestByCoroutineTest() {
        lifecycleScope.launch {
            val s = testService.getIpCour("zh-cn")
            Log.e("------", "$s")
            tv?.text = "$s"
        }
    }

    private fun requestTestByObserver() {
        Log.e("------", "0000  ")
        testService.getIp("zh-cn").call(this) { isSuccess, data, throwable ->
            val s = "$isSuccess :  ${data.toString()}   $throwable"
            Log.e("------", s)
            tv?.text = s
        }
        Log.e("------", "11111")
    }

    private fun requestTestByCancelAbleObserver() {
        var compo: RequestCancelable? = null
        compo = testService.getIp("zh-cn").call { isSuccess, data, throwable ->
            val s = "$isSuccess :  ${data.toString()}   $throwable"
            Log.e("------", s)
            tv?.text = s
            compo?.cancel()
        }
    }
}
