package com.zj.apiTest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.zj.api.BaseApi
import com.zj.api.utils.LoggerInterface
import com.zj.apiTest.config.TestApi
import kotlinx.android.synthetic.main.activity_main.*

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
            TestApi.getIp("zh-CN") {
                tv?.text = it
            }

            //TestApi.test()
        }
    }
}
