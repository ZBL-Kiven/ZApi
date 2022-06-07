<p align="center" >
   <a>
     <img src = "https://github.com/ZBL-Kiven/BaseApi/blob/master/raw/title_screen.png"/>
   </a>
   <br>
   <br>
   <a href = "https://github.com/ZBL-Kiven">
     <img src = "https://img.shields.io/static/v1?label=By&message=ZBL-Kiven&color=2af"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven">
      <img src = "https://img.shields.io/static/v1?label=platform&message=Android&color=6bf"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven">
      <img src = "https://img.shields.io/static/v1?label=author&message=ZJJ&color=9cf"/>
  </a>
  <a href = "https://developer.android.google.cn/jetpack/androidx">
      <img src = "https://img.shields.io/static/v1?label=supported&message=Coroutine&color=8ce"/>
  </a>
  <a>
      <img src = "https://img.shields.io/static/v1?label=minVersion&message=5.0&color=cce"/>
  </a>
  <a>
      <img src = "https://img.shields.io/static/v1?label=newest version&message=1.3.5&color=DAC"/>
  </a>
</p>

## Introduction：

###### ZApi 是基于 Okhttp3 开发的网络框架，可以非常便捷的使用 ，并在变态级需求下仍可轻松保持代码的简洁高效。

###### 已有多个千万用户级项目使用，详情可参见下方 Features 条目。

## Features：

* 为降低学习成本，此框架 Http 部分注解及解析与 Retrofit 保持一致。
* 支持：Kotlin 协程。
* 支持：自定义 静/动态 BaseUrl 设定。
* 支持：自定义 静/动态 Header 设定。
* 支持：证书 单独/全局 配置。
* 支持：自定义 HttpClient、RequestFactory 、JsonParser/Converter 、RequestCallBack 等内置处理器自定义。
* 支持：单个接口单独注解超时时间 [@ApiHandler](#api_handler)。
* 支持：单个接口单独注解 ErrorHandler 回调线程 [@ApiHandler](#api_handler)。
* 支持：注解级可传参 [Mock](#mock) 。
* 支持：添加/自定义 Logger ，按自己的个性化输出日志。
* 支持：为上传或下载提供基于流监测的精确进度回调。
* 支持：UploadInterceptor 拦截 / 修改 / 代理 ；上传请求。
* 支持：DownloadInterceptor 拦截 / 修改 / 代理 ；下载请求。
* 支持：便捷下载、上传 及其进度回调。
* 支持：流量监控。
* 支持：配置 全局 / 模块 / 单次； 请求异常处理器 [ErrorHandler](#errorHandler) 。
* 支持：配置 全日志系统 。
* 支持：异常处理器拦截、中断、改变返回结果 。
* 支持：请求方法带自定义参数，[@EHParams](#EHParams)注解实现请求过程参数透传。
* 支持：全局、模块、单次 请求随页面生命周期自动绑定 。
* 支持：Cancelable 手动取消 。

## demo：

使用 Android 设备下载 [APK](https://github.com/ZBL-Kiven/BaseApi/blob/master/raw/app-debug.apk) 安装包安装 demo
即可把玩。

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

implementation 'com.zj.repo:api:#version'
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

> 因为广谱共识的原因，解析注解这一块仍然使用 Retrofit 的 http 注解，便于新生学习、老鸟使用或升级网络框架。
>
> so ，依旧像 Retrofit 一样创建你的 Interface ，并使用更丰富的 Api：
>
> 注意：如果你的项目已有 Retrofit ，使用时需注意，ZApi 中与 Retrofit 相似的 Api 仅是为了方便学习及转换，在 ZApi 中相应函数的包名并非 retrofit.xxx , 而是 com.zj.ok3.http.* 。

```kotlin
 @GET("json/")
fun getIp(@Query("lang") lang: String): Observable<Any>
```

返回对象为 Observable ，可直接 call 获取结果。

```kotlin
@GET("json/")
suspend fun getIp(@Query("lang") lang: String): String?

@GET("json/")
suspend fun getIp(@Query("lang") lang: String): SuspendObservable<Any>?
```

1. SuspendObservable 含有 Suspend 挂起函数 ，返回值为 SuspendObservable 的，将同步返回值为 SuspendObservable 的数据

2. 含有 Suspend 挂起函数 ，返回值直接为所需类型的，将直接返回数据

> <a id = "s03"> 基于 Interfaces 创建 Service：</a>

```kotlin
val testService: TestService = ZApi.create(TestService::class.java, ApiErrorHandler) // 可选，是否为此 Service 添加 ErrorHandler 。
    .baseUrl(Constance.getBaseUrl()) // 自定义 BaseUrl ，可变 。
    .header(Constance.getHeader()) // 自定义 Header ， 可变 。
    .debugAble(boolean) // 是否启用全日志系统 （不含流量分析系统），默认关闭 。
    .logLevel(level) // LogLevel:这个枚举设置了扩展函数，你可以这样使用: HEADERS + BASIC + RESULT_BODY
    .mockAble(boolean) // 本 Service 是否启用 Mock 功能, 默认开启 。
    .certificate(Arra<InputStream>) // 添加证书 。
    .build() // 返回的 testService 为接口声明的动态代理，可直接访问接口内方法 。
```

注意：

1、建议 一个 Interface 文件对应初始化一个 ZApi ，这样做是为了让自己更清晰的区分模块化和数据解藕。比如 UserCenterService , LaunchService , AssetsService 等。每个 Service 都会动态代理 Create 传入的 Interfaces ，达到直接便捷 [访问接口](#s05) 的目的。

2、视自身使用习惯而定，它可以初始化在静态或非静态类 ，并不会影响它里面的某个接口自动与调用接口的生命周期函数绑定。

> 请求接口 - 使用

#### 1. 下载

```kotlin
ZApi.Downloader.with(url, f).callId("111") // 设置标识 ID
    .errorHandler(ApiErrorHandler) // 设置错误处理器
    .timeout(3000) // 设置超时时间
    .observerOn(ZApi.IO) // 设置在什么线程回调结果，默认主线程。
    .start(object : DownloadListener { 
        // fun onStart(callId) 开始
        // fun onCompleted(callId，absolutePath: String) 完成
        // fun onProgress(callId，i: Int) 进度 0 - 100
        // fun onError(callId，e: Throwable?, isCanceled: Boolean = false) 错误
    })
```

### 2. 上传

```kotlin
ZApi.Uploader.with(url).errorHandler(ApiErrorHandler).setFileInfo(fInfo).header(header).addParams(map).start(object : FileUploadListener { 
    // onCompleted(uploadId, fileInfo, totalBytes)  请求完成。
    // onError(uploadId, fileInfo, exception, errorBody) 出现异常，在 EH 未拦截的情况下回调。
    // onProgress(uploadId, fileInfo, progress: Int, contentLength) 进度变化（已做防抖。
    // onSuccess(uploadId, body, totalBytes) 在 EH 拦截处理或放行后回调。
    // onUploaded(uploadId, fileInfo, contentLength) 当一个文件完成上传。
})
```

### 2.1 多任务上传

``` kotlin
//与单文件上传不同的是，setFile 变成了 addFile。
ZApi.MultiUploader.with(url).addFile(fInfo)...start(object : FileUploadListener {}
```

### 3. GET/POST/DELETE/PATCH ....

3.1 ：协程使用

```kotlin
lifecycleScope.launch { //协程使用
    val result = testService.getIpCour("zh-cn") //完成网络请求
    result.data //do something
}
```

3.2 CallBack 使用

```kotlin
testService.getIp("zh-cn").call(LifecycleOwner) { isSuccess, data, throwable, handled -> //handledData ->
    //完成网络请求，do something
}
```

注：LifecycleOwner 为选填项 ，传入后此网络请求将自动与生命周期绑定，页面结束时请求会自动取消，不传入则请求与生命周期无关。

#### 至此， ZApi 的基本使用已经讲诉完成，若你是项目规划者、DevLeader、或想要更深入、更细节、更多的支持，以下的内容可能会钓起你的很多的扩展想法～

### 进阶

### <a name="EHParams">@EHParams</a>

```kotlin
/**
 * @EHParams 注解 ：
 * 被注解的参数将在实际请求时从生成代理中剥离，它不会真正参与接口传递，因此不会对接口结构、响应、性能造成任何影响。
 * */
@GET("json/")
fun getIpCour(@Query("lang") lang: String, @EHParams("uid") uid: String): Observable<Any>?
```

EHParams :

> 此参数默认出现在 [ErrorHandler](#errorHandler) 、[MockAble.getMockData()](#mock)  中，为当前作业的接口所带的参数 Map 集，其中包含所有的含 @EHParams 注解的参数及所有请求的默认参数。



### <a name="api_handler">@Apihandler</a>

```kotlin
/**
 * api handler 主要是对单个接口进行扩展的功能性注解。
 * id ：单独对这个接口进行标记，此标记将伴随之后的任何与之相关的曝光点。
 * timeOut : 指定单个接口的超时时间，该设置会覆盖 ZApi Builder 的全局设置。
 * successEHScope ：当设置了 ErrorHandler 时，你希望 EH 的成功拦截器在什么线程回调，当然这不影响它最终回调线程的设置。
 * errorEHScope ： 同上，此处为当此接口发生任何例外的时候，拦截器将在什么线程回调。
 **/
@ApiHandler(timeOut = 1000, successEHScope = ZApi.MAIN, errorEHScope = ZApi.IO, id = "first_test")
@GET("json/")
suspend fun getIpCourSimple(@Query("lang") lang: String): Any?
```

### <a  name="mock">@Mock</a>

``` kotlin
@Mock(MockTest::class) //mock
@GET("json/")
fun getIp(@Query("lang") lang: String): Observable<Any>
```

为任意接口定义 Mock 注解，含有此注解的接口在实际请求时会自动在注解的 Mock 类 (此处为 MockTest）调用你写好的返回结果，且不会发起网络请求和检查 Header 、Url
、证书等的错误。

删除此注解 ，或 [创建 Service ](#s03) 时设置 mockAble(false) 即不再加载 Mock 类，完全恢复正常请求流程。

此注解适用于所有通过此网络框架进行请求的方法，不限返回类型和调用类型。

### <a name = "errorHandler"> ErrorHandler</a> 异常处理器

ps:
1、此处理器内方法回调的线程可配置，参见 [@ApiHandler](#api_handler)
2、@param ehParams 请求时附加的信息，参见[@EHParams](#EHParams)

```kotlin
interface ErrorHandler {
    //提前处理错误信息，可用于错误统一处理或忽略。
    //返回 true 即拦截此次错误，下游不会收到。
    //返回 false 时下游仍能收到错误，且 Second 返回后下游可得到 handledData 数据，便于对特殊情况作处理。
    open suspend fun interruptErrorBody(throwable: ApiException?, ehParams: EHParam): Pair<Boolean, Any?>

    //是否拦截成功后的数据，支持返回空或修改
    open suspend fun <R> interruptSuccessBody(id: String, code: Int, data: R?, ehParams: EHParam): R?
}
```

### 设置流量监视器 （精确至 Byte）

```kotlin
ZApi.setFlowsListener(TestService::class.java, object : LoggerInterface {
    override fun onSizeParsed(fromCls: String, isSend: Boolean, size: Long) { //main thread , do something 
    }
})
```

传人 TestService 表示此监听器仅会回调所有发生在 TestService 内的所有接口的 上/下载流量 ，包括文件的 上传/下载 。

```kotlin
ZApi.setGlobalFlowsListener(object : LoggerInterface {})
```

与上一样，但它是全局的，即任何 Service 发生的流量记录都会回调至此，包括文件的 上传/下载 。

#### 更多的日志查看

当 DebugAble 为允许时，可以通过配置 LogLevel 设置更随你心意的 Log 打印和全日志系统搜集策略。

### Contributing

Contributions are very welcome 🎉

### Licence :

Copyright (c) 2022 io.github zjj0888@gmail.com<br>
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.<br>
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.