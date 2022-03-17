<p align="center" >
   <img src = "https://github.com/ZBL-Kiven/BaseApi/blob/master/raw/title_screen.png"/>
   <br>
   <br>
   <a href = "https://github.com/ZBL-Kiven/">
   <img src = "https://img.shields.io/static/v1?label=By&message=ZBL-Kiven&color=2af"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven/album">
      <img src = "https://img.shields.io/static/v1?label=platform&message=Android&color=6bf"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven">
      <img src = "https://img.shields.io/static/v1?label=author&message=ZJJ&color=9cf"/>
  </a>
  <a href = "https://developer.android.google.cn/jetpack/androidx">
      <img src = "https://img.shields.io/static/v1?label=supported&message=Coroutine&color=8ce"/>
  </a>
  <a href = "https://www.android-doc.com/guide/components/android7.0.html">
      <img src = "https://img.shields.io/static/v1?label=minVersion&message=5.0&color=cce"/>
  </a>
</p>


## Introduction：

###### ZApi  是基于 Okhttp3 开发的网络框架，可以非常便捷的使用下载、上传及 GET 、POST、DELETE等所有请求，支持全日志收集系统、流量监测系统、异常集散系统、可选的生命周期自动绑定功能、支持协程使用，已有多个千万用户级项目使用，其全局异常分发器和类似 Interceptor 的异常处理架构，使其可以非常直观快速的调试任何接口，详情可参加下方使用案例。

## Features：

* 支持：模块化单独配置的单独初始化
* 支持：自定义 静/动态 BaseUrl 设定。
* 支持：自定义 静/动态 Header 设定。
* 支持：证书 单独/全局 配置
* 支持：自定义 HttpClient、RequestFactory 、JsonParser/Converter 、RequestCallBack 等内置处理器。
* 支持：自定义 使用或不使用 Retrofit Factory 。
* 支持：添加/自定义 LogInterceptor ，ProgressInterceptor , RequestInterceptor 等 。
* 支持：下载、上传 进度回调。
* 支持：流量监控 。
* 支持：配置 全局 / 模块 / 单次 请求 异常处理器 。
* 支持：配置 全日志系统 。
* 支持：异常处理器拦截、中断、改变返回结果 。
* 支持：协程调用 。
* 支持：全局、模块、单次 请求随生命周期自动绑定 。
* 支持：注解级 Mock 。
* 支持：Cancelable 手动取消请求 。

## demo：

使用 Android 设备下载 [APK](https://github.com/ZBL-Kiven/BaseApi/blob/master/raw/app-debug.apk) 安装包安装 demo 即可把玩。

## Installation :

ZApi 已发布至私有仓库，并将在近期开源至 MavenCentral 。

你可以使用如下方式安装它：

> by dependencies:

```grovy
maven {
    allowInsecureProtocol = true
    credentials {
         username 'usr'
         password '123456'
    }
    url 'https://www.jqyard.cn:8081/repository/zjj-release/'
}

implementation 'com.zj.repo:loading:1.2.9'
```

> by [aar](https://github.com/ZBL-Kiven/BaseApi/blob/master/raw/zj-api-release.aar) import:

```
点击下载 AAR ，可以依赖进项目或将其上传至你的 Repositories 。
```

> by [module.zip](https://github.com/ZBL-Kiven/BaseApi/archive/refs/heads/master.zip) copy:

```
copy the module 'zj-api' into your app

implementation project(":zj-api")

```

## Usage:

> 像 Retrofit 一样创建你的 Interface ，使用更丰富的 Api：

```kotlin
 @GET("json/")
 fun getIp(@Query("lang") lang: String): Observable<Any>
```

[返回对象为 Observable ，可直接 call 获取结果。](#1)

```kotlin
 @GET("json/")
 suspend fun getIp(@Query("lang") lang: String): String?

@GET("json/")
    suspend fun getIp(@Query("lang") lang: String): SuspendObservable<Any>?
```

1.[ : SuspendObservable](#s01) [含有 Suspend 挂起函数 ，返回值为 SuspendObservable 的，将同步返回值为 SuspendObservable 的数据](#s02)

2.[含有 Suspend 挂起函数 ，返回值直接为所需类型的，将直接返回数据](#s02)

> Mock 的使用

``` kotlin
   @Mock(MockTest::class) //mock
   @GET("json/")
   fun getIp(@Query("lang") lang: String): Observable<Any>
```

为任意接口定义 Mock 注解，含有此注解的接口在实际请求时会自动在注解的 Mock 类 (此处为 MockTest）调用你写好的返回结果，且不会发起网络请求和检查 Header 、Url 、证书等的错误。

删除此注解 ，或 [创建 Service ](#s03) 时设置 mockAble(false) 即不再加载 Mock 类，完全恢复正常请求流程。

此注解适用于所有通过此网络框架进行请求的方法，不限返回类型和调用类型。

> <a id = "s03"> 基于 Interfaces 创建 Service：</a>

```kotlin
1、val testService: TestService = ZApi
.create(TestService::class.java, ApiErrorHandler) // 可选，是否为此 Service 添加 ErrorHandler 。
.baseUrl(Constance.getBaseUrl()) // 自定义 BaseUrl ，可变 。
.header(Constance.getHeader()) // 自定义 Header ， 可变 。
.debugAble(boolean) // 是否启用全日志系统 （不含流量分析系统），默认关闭 。
.mockAble(boolean) // 本 Service 是否启用 Mock 功能, 默认开启 。
.certificate(Arra<InputStream>) // 添加证书 。
.build() // 返回的 testService 为接口声明的动态代理，可直接访问接口内方法 。
```

注意：

1、建议 一个 Interface 文件对应初始化一个 Service ，这样做是为了更好的支持模块化和数据解藕。比如 UserCenterService , LaunchService , AssetsService 等。每个 Service 都会动态代理 Create 传入的 Interfaces ，达到直接便捷 [访问接口](#s05) 的目的。

2、视自身使用习惯而定，它可以初始化在静态或非静态类 ，并不会影响它里面的某个接口自动与调用接口的生命周期函数绑定。

> 请求接口 - 使用

#### 1. 下载

```kotlin
ZApi.download(f, url, object : DownloadListener() {  // 或实现默认方法的 SimpleDownloadListener
    fun onStart() //开始
    fun onCompleted(absolutePath: String) //完成
    fun onProgress(i: Int) //进度 0 - 100
    fun onError(e: Throwable?, isCanceled: Boolean = false) //错误
})
```

### 2. 上传

```kotlin
val progressBody = ProgressRequestBody(rq, it.uploadIndex, mProgressListener)
val body = MultipartBody.Part.createFormData(paramName, fileName, progressBody)
testService.uploadFile(){isSuccess, data, throwable -> //handledData -> 通过 ErrorHandler 返回的其他数据
   //do something
}
```

### 3. GET/POST/DELETE/PATCH ....

1. 协程使用

```kotlin
lifecycleScope.launch { //协程使用
   val result = testService.getIpCour("zh-cn") //完成网络请求
   result.data //do something
}
```

2. CallBack 使用

```kotlin
testService.getIp("zh-cn").call(LifecycleOwner?) { isSuccess, data, throwable, handled -> //handledData ->
    //完成网络请求，do something
}
```

注：LifecycleOwner 为选填项 ，传入后此网络请求将自动与生命周期绑定，页面结束时请求会自动取消，不传入则请求与生命周期无关。

>ErrorHandler 异常处理器

```kotlin
interface ErrorHandler{
    //提前处理错误信息，可用于错误统一处理或 忽略。
  	//返回 true 即拦截此次错误，下游不会收到。
    //返回 false 时下游仍能收到错误，且 Second 返回后下游可得到 handledData 数据，便于对特殊情况作处理。
		fun interruptErrorBody(throwable: HttpException?): Pair<Boolean, Any?> 
 
    //是否拦截成功后的数据，支持返回空或修改
		fun <R> interruptSuccessBody(data: R?): R?  
}
```

> 设置流量监视器 （精确至 Byte）

```kotlin
ZApi.setFlowsListener(TestService::class.java, object : LoggerInterface {
     override fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) {
           //do something 
     }
})
```

传人 TestService 表示此监听器仅会回调所有发生在 TestService 内的所有接口的 上/下载流量 ，包括文件的 上传/下载 。

```kotlin
ZApi.setGlobalFlowsListener(object : LoggerInterface)
```

与上一样，但它是全局的，即任何 Service 发生的流量记录都会回调至此，包括文件的 上传/下载 。



### Contributing

Contributions are very welcome 🎉

### Licence :

Copyright (c) 2022 io.github zjj0888@gmail.com<br>
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.<br>
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.