package com.zj.apiTest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zj.api.BaseApi
import com.zj.api.downloader.SimpleDownloadListener
import com.zj.api.utils.LoggerInterface
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BaseApi.setLoggerInterface(TestService::class.java, object : LoggerInterface {
            override fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) {
                Log.e("------", "from :$fromCls , onSizeParsed in thread : ${Thread.currentThread().name}: isSend = $isSend , size = $size")
            }
        })

        tv?.setOnClickListener {
            val f = File(cacheDir, "11221.aac")
            val url = "https://media.clipclaps.com/a/20211123/a/7/b/a7b04cef4dc74f8d9c9cb927b0752ba5.aac"
            BaseApi.download(f, url, object : SimpleDownloadListener() {
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

            //            TestApi.getIp("zh-CN") {
            //                tv?.text = it
            //            }
        }
    }
}
