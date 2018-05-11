package com.shtoone.aqm.network

import android.util.Log
import com.shtoone.aqm.base.BaseApplication
import com.shtoone.aqm.base.BaseObject
import com.shtoone.aqm.features.bigfileupload.ChunkBody
import com.shtoone.aqm.features.bigfileupload.UploadChunkFileResponse
import com.shtoone.aqm.features.location.UploadLocation
import com.shtoone.aqm.features.location.UploadLocationResponse
import com.shtoone.aqm.features.login.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
class RetrofitManager {
    //私有构造,单例模式
    private constructor() {
        initRetrofit()
    }

    companion object {
        val TAG = "RetrofitManager"
        var retrofitManager: RetrofitManager? = null
        var retrofit: RetrofitService? = null

        fun getInstance(): RetrofitManager? {
            if (retrofitManager == null) {
                synchronized(RetrofitManager::class.java) {
                    //双锁
                    if (retrofitManager == null) {
                        retrofitManager = RetrofitManager()
                    }
                }
            }
            return retrofitManager
        }

        fun login(amqSN: String, observable: Observers.LoginObserver) {
            retrofit?.loginHelmet(amqSN)
                    ?.onErrorReturn(object : Function<Throwable, LoginResponse> {
                        //会忽略onError调用，不会将错误传递给观察者
                        override fun apply(p0: Throwable): LoginResponse? {
                            //作为替代，它会发发射一个特殊的项并调用观察者的onCompleted方法(不回调onNext和onError)
                            Log.e(TAG, ">>>login>>>>网络异常:" + p0)
                            return null
                        }
                    })
                    ?.subscribeOn(Schedulers.io())//IO线程订阅
                    ?.observeOn(AndroidSchedulers.mainThread())//主线程回调
                    ?.subscribe(observable)
        }


        fun uploadLocation(body: UploadLocation, observable: Observers.LocationObserver) {
            retrofit?.uploadLocation(body)
                    ?.onErrorReturn(object : Function<Throwable, UploadLocationResponse> {
                        //会忽略onError调用，不会将错误传递给观察者
                        override fun apply(p0: Throwable): UploadLocationResponse? {
                            //作为替代，它会发发射一个特殊的项并调用观察者的onCompleted方法(不回调onNext和onError)
                            Log.e(TAG, ">>>login>>>>网络异常:" + p0)
                            return null
                        }
                    })
                    ?.subscribeOn(Schedulers.io())//IO线程订阅
                    ?.observeOn(AndroidSchedulers.mainThread())//主线程回调
                    ?.subscribe(observable)
        }


        fun uploadChunkFile(path: String, body: ChunkBody, observable: Observers.UploadChunkFileObserver) {

            var file = File(path)
            var builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)//表单类型
                    .addFormDataPart("uuid", body?.uuid)
                    .addFormDataPart("chunk", body?.chunk?.toString())
                    .addFormDataPart("chunks", body?.chunks?.toString())
                    .addFormDataPart("fileType", "666")
//                    .addFormDataPart("lat", "lat")
                    .addFormDataPart("lng", "lng")
                    .addFormDataPart("lat", "lat")
                    .addFormDataPart("address", "address")
                    .addFormDataPart("userName", BaseApplication.userName)
                    .addFormDataPart("time", "2017-12-27 10:24:49")//表单类型

            var body = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            builder.addFormDataPart("file", file.getName(), body)
            var parts = builder.build().parts()

            retrofit?.uploadChunkFile(BaseApplication.sn, parts)
                    ?.onErrorReturn(object : Function<Throwable, UploadChunkFileResponse> {
                        //                    ?.onErrorReturn(object : Function<Throwable, BaseObject<String>> {
                        //会忽略onError调用，不会将错误传递给观察者
                        override fun apply(p0: Throwable): UploadChunkFileResponse? {
//                        override fun apply(p0: Throwable): BaseObject<String>? {
                            //作为替代，它会发发射一个特殊的项并调用观察者的onCompleted方法(不回调onNext和onError)
                            Log.e(TAG, ">>>login>>>>网络异常:" + p0)
                            return null
                        }
                    })
                    ?.subscribeOn(Schedulers.io())//IO线程订阅
                    ?.observeOn(AndroidSchedulers.mainThread())//主线程回调
                    ?.subscribe(observable)
        }


    }

    fun initRetrofit() {
        //拦截器（打印网络请求log）
        var logInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor(HttpLogger())
        logInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        var httpInterceptor = HttpInterceptor()
        var okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logInterceptor)//添加拦截器
//                .addInterceptor(httpInterceptor)//添加请求头
                .build()

//        okHttpClient?.


        retrofit = Retrofit.Builder()
                .baseUrl(RetrofitService.BaseURL)
                .addConverterFactory(GsonConverterFactory.create())//配置gson转换
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//配置rxjava转换
                .client(okHttpClient)
                .build()
                .create(RetrofitService::class.java)//创建接口实例
    }

    /**
     * 日志拦截器
     */
    class HttpLogger : HttpLoggingInterceptor.Logger {
        override fun log(message: String?) {
            Log.e("HttpLogInfo", message)
        }

    }

    class HttpInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain?): okhttp3.Response {
            Log.e("HttpInterceptor", "")
            var builder = chain?.request()?.newBuilder()
            var requst = builder?.addHeader("content-type", "text/html;charset=UTF-8")?.build()
            return chain?.proceed(requst)!!
        }
    }
}