package com.zj.apiTest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zj.apiTest.config.TestApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv?.setOnClickListener {
            //            TestApi.getIp("zh-CN") {
            //                tv?.text = it
            //            }
            TestApi.test()
        }
    }
}
