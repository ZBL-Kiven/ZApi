package com.zj.apiTest

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zj.api.ZApi
import com.zj.api.call
import com.zj.api.downloader.DownloadListener
import com.zj.api.exception.ApiException
import com.zj.api.interceptor.plus
import com.zj.api.interfaces.RequestCancelable
import com.zj.api.uploader.FileInfo
import com.zj.api.uploader.FileUploadListener
import com.zj.api.utils.LoggerInterface
import kotlinx.coroutines.launch
import java.io.File


@Suppress("unused")
class MainActivity : AppCompatActivity() {

    private var curDownloadedFile = ""

    private val testService = ZApi.create(TestService::class.java, ApiErrorHandler).baseUrl(Constance.getBaseUrl()).header(Constance.getHeader()).build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ZApi.addFlowsListener(TestService::class.java, object : LoggerInterface {
            override fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long, msg: String) {
                Log.e("------", "from :$fromCls , onSizeParsed in thread : ${Thread.currentThread().name}: isSend = $isSend , size = $size , msg = $msg")
            }
        })
    }

    /**
     * 使用 ZApi 下载远程文件
     * */
    fun downloadTest(v: View) {
        v as TextView
        val s = "downloading"
        v.text = s
        val f = File(cacheDir, "ZApi_download_test.png")
        val url = "https://img0.baidu.com/it/u=1032173647,760525262&fm=253&fmt=auto&app=120&f=JPEG?w=1000&h=562"
        ZApi.Downloader.with(url, f).callId("111").errorHandler(ApiErrorHandler).timeout(3000).start(object : DownloadListener {
            override suspend fun onCompleted(callId: String, absolutePath: String) {
                v.text = absolutePath
                curDownloadedFile = absolutePath
            }

            override suspend fun onError(callId: String, e: Throwable?, isCanceled: Boolean) {
                v.text = e?.message
                e?.printStackTrace()
            }
        })
    }


    fun uploadTest(v: View) {
        val timeStamp = System.currentTimeMillis()
        val map = mutableMapOf<String, String>()
        map["type"] = "image"
        map["sign"] = "$timeStamp;151147;image".md5()
        val f = File(externalCacheDir, "ZApi_download_test.png")
        val fInfo = FileInfo(f.name, "file", f)
        val url = Constance.getBaseUrl() + "/im/upload/file"
        val header = Constance.getHeader() + mapOf("Content-Type" to "multipart/form-data", "userId" to "151147", "token" to "sanhe12345", "timeStamp" to "$timeStamp")
        ZApi.Uploader.with(url).errorHandler(ApiErrorHandler).setFileInfo(fInfo).header(header).addParams(map).start(object : FileUploadListener {

            override fun onError(uploadId: String, fileInfo: FileInfo?, exception: ApiException?, errorBody: Any?) {
                (v as? TextView)?.text = exception?.message
            }

            override fun onSuccess(uploadId: String, body: Any?, totalBytes: Long) {
                (v as? TextView)?.text = body.toString()
            }
        })
    }

    fun requestByCoroutineSimpleTest(v: View) {
        v as TextView
        lifecycleScope.launch {
            val obj = testService.getIpCourSimple("zh-cn")
            v.text = "$obj"
        }
    }

    fun requestByCoroutineTest(v: View) {
        v as TextView
        lifecycleScope.launch {
            val s = testService.getIpCour("zh-cn", "121212", "sadasd")
            v.text = "$s"
        }
    }

    fun requestTestByObserver(v: View) {
        v as TextView
        testService.getIpMock("zh-cn", "mock test eh-param").call(this) { isSuccess, data, throwable, handled ->
            val s = "$isSuccess :  ${data.toString()}"
            v.text = s
        }
    }

    fun requestTestByCancelAbleObserver(v: View) {
        v as TextView
        var compo: RequestCancelable? = null
        compo = testService.getIp("zh-cn").call { isSuccess, data, throwable, handled ->
            val s = "$isSuccess :  ${data.toString()}   ${throwable?.message}"
            v.text = s
            compo?.cancel()
        }
    }
}