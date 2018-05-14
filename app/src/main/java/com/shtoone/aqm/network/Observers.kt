package com.shtoone.aqm.network

import android.util.Log
import com.shtoone.aqm.base.BaseObject
import com.shtoone.aqm.features.bigfileupload.UploadChunkFileResponse
import com.shtoone.aqm.features.location.UploadLocationResponse
import com.shtoone.aqm.features.login.LoginResponse
import com.shtoone.aqm.features.news.UnknowVoiceInfo
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
open class Observers {
    open class BaseObserver<T> : Observer<T> {
        open val TAG = this.javaClass.name
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "======*********===========>>>>>>>:EXCEPTION:" + e?.toString())
            e?.stackTrace?.forEach {
                Log.e(TAG, "EEEEEE>>>>>>>:" + it)
            }
        }

        override fun onNext(t: T) {
        }

    }

    open class LoginObserver : BaseObserver<LoginResponse>() {}
    open class UploadChunkFileObserver : BaseObserver<UploadChunkFileResponse>() {}
    open class LocationObserver : BaseObserver<UploadLocationResponse>() {}
    open class VoiceInfoObserver : BaseObserver<UnknowVoiceInfo>() {}
    open class NetworkObserver : BaseObserver<ResponseBody>() {}
    open class PollingObserver : BaseObserver<Long>() {}

}