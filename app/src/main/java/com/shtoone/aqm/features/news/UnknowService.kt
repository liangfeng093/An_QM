package com.shtoone.aqm.features.news

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.amap.api.mapcore.util.it
import com.shtoone.aqm.base.BaseApplication
import com.shtoone.aqm.interfaces.Actions
import com.shtoone.aqm.main.ChangeStatusEvent
import com.shtoone.aqm.network.Observers
import com.shtoone.aqm.network.RetrofitManager
import com.shtoone.aqm.network.RetrofitService
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by mzf on 2017/12/28.
 * Email:liangfeng093@gmail.com
 */
class UnknowService : Service() {

    //    val TAG = "UnknowService"
    val TAG = this.javaClass.name
    var mediaPlayer: MediaPlayer = MediaPlayer()

    var tcrId: String? = null

    var pathList: MutableList<File>? = null
    var index = 0
    var player: MediaPlayer? = MediaPlayer()
    var isRequest = true
    var isOver = true
    //    var observable: Unit? = null
    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, ">>>>>>>onCreate")
        pathList = mutableListOf<File>()
        Observable.interval(0, 1, TimeUnit.SECONDS)//两秒发送一次
                .subscribeOn(Schedulers.io())//在io线程进行网络操作
                .doOnNext(object : Consumer<Long> {
                    override fun accept(t: Long?) {
                        if (isRequest) {
                            isRequest = false
//                            Log.e(TAG, "第 " + t + " 次轮询");
                            try {
                                getVoice()
                            } catch (e: Exception) {
                                e?.stackTrace?.forEach {
                                    Log.e(TAG, ">>>>>>>Exception:" + it)
                                }
                            }
                        }
                    }

                }).subscribe(object : Observers.PollingObserver() {
            //订阅之后才会触发doOnNext
            override fun onComplete() {
                Log.e(TAG, "轮询器>>>onComplete>>>>:")
            }
        })

    }

    var size = 0
    var list = mutableListOf<VoiceInfo>()
//    var list = mutableListOf<UnknowVoiceInfo.Obj>()

    private fun getVoice() {

        RetrofitManager.getVoiceInfo(BaseApplication.userName, object : Observers.VoiceInfoObserver() {
            override fun onError(e: Throwable) {
                super.onError(e)
                isRequest = true
                e?.stackTrace?.forEach {
                    Log.e(TAG, "getVoiceInfo>>>>>>>Exception:" + it)
                }
            }

            override fun onNext(t: UnknowVoiceInfo) {
                try {
                    size = t?.obj?.size!!
//                Log.e(TAG, ">>>>>>>音频size：" + size)
                    Log.e(TAG, "###$$$###>>>>>>>音频size:" + size)
                    t?.obj?.let {
                        t?.obj?.forEach {
                            Log.e(TAG, "=========>>>>>>>TCRSate：" + it?.TCRSate)
                            when (it?.TCRSate) {
                                Actions.TCR_STATUS_MAN -> {
                                    BaseApplication.singleCallLastMp3Url = RetrofitService.mp3Url + it?.TCRContent
                                    BaseApplication.otherUserName = it?.tcrfrom!!
                                    BaseApplication.otherRealName = it?.tcrfromReal!!
                                    BaseApplication.lastCallLastMp3Url = BaseApplication.singleCallLastMp3Url
//                                    EventBus.getDefault().post(ChangeStatusEvent(Actions.TCR_STATUS_MAN))
                                    Log.e(TAG, ">>>RetrofitManager.getVoiceInfo>>>>singleCallLastMp3Url:" + BaseApplication.singleCallLastMp3Url)
                                }
                                Actions.TCR_STATUS_TEMP_TG -> {
                                    BaseApplication.TempTgCallLastMp3Url = RetrofitService.mp3Url + it?.TCRContent
                                    BaseApplication.lastCallLastMp3Url = BaseApplication.TempTgCallLastMp3Url
                                    BaseApplication.tempTeamName = it?.TeamNamels!!
//                                    EventBus.getDefault().post(ChangeStatusEvent(Actions.TCR_STATUS_TEMP_TG))
                                    Log.e(TAG, "Actions.TCR_STATUS_TEMP_TG>>>>>>>it?.TCRTeamCode:" + it?.TCRTeamCode)
                                    var data = it
                                    BaseApplication.tempTeamCode = data?.TCRTeamCode!!
                                    it?.TCRTeamCode?.let {
                                    }
                                    Log.e(TAG, ">>>RetrofitManager.getVoiceInfo>>>>TempTgCallLastMp3Url:" + BaseApplication.TempTgCallLastMp3Url)
                                }
                                Actions.TCR_STATUS_TG -> {
                                    BaseApplication.TgCallLastMp3Url = RetrofitService.mp3Url + it?.TCRContent
                                    BaseApplication.lastCallLastMp3Url = BaseApplication.TgCallLastMp3Url
                                    BaseApplication.teamName = it?.TeamName!!
                                    EventBus.getDefault().post(ChangeStatusEvent(Actions.TCR_STATUS_TG))
                                    Log.e(TAG, "Actions.TCR_STATUS_TG>>>>>>>it?.TCRTeamCode:" + it?.TCRTeamCode)
                                    var data = it
                                    it?.TCRTeamCode?.let {
                                        BaseApplication.teamCode = data?.TCRTeamCode!!
                                    }
                                    Log.e(TAG, ">>>RetrofitManager.getVoiceInfo>>>>TgCallLastMp3Url:" + BaseApplication.TgCallLastMp3Url)
                                }

                                Actions.TCR_STATUS_NOTICE -> {
                                    EventBus.getDefault().post(ChangeStatusEvent(Actions.TCR_STATUS_NOTICE))
                                    BaseApplication.noticeCallLastMp3Url = RetrofitService.mp3Url + it?.TCRContent
                                }
                                Actions.TCR_STATUS_TRAIN -> {
                                    EventBus.getDefault().post(ChangeStatusEvent(Actions.TCR_STATUS_TRAIN))
                                    BaseApplication.trainCallLastMp3Url = RetrofitService.mp3Url + it?.TCRContent
                                }
                            }
                        }
                    }
                    index = 0
                    list?.clear()//清除缓存
                    list = t?.obj!!
                    player?.let {
                        player?.setOnCompletionListener {
                            tcrId?.let {
                                infoReceived()
                            }
                        }
                    }
                    if (size == 0) {
                        Log.e(TAG, "=============>>>>>>>onNext>>>>没有音频文件size")
                        isRequest = true
                    } else {
                        playVoice(size, list)
                    }
                } catch (e: Exception) {
                    isRequest = true
                    e?.stackTrace?.forEach {
                        Log.e(TAG, ">>>>>>>Exception:" + it)
                    }
                }
            }

        })
    }

    private fun infoReceived() {
        RetrofitManager.infoReceived(tcrId!!, object : Observers.NetworkObserver() {
            override fun onError(e: Throwable) {
                if (size!! > index) {
                    playVoice(size, list)
                } else {
                    isRequest = true
                }
            }

            override fun onNext(t: ResponseBody) {
                Log.e(TAG, "=============setOnCompletionListener>>>>>>>onComplete>>>>播放完毕第:" + (index - 1) + "条")
                //                                    Logger.e("已播放")
                if (size!! > index!!) {
                    playVoice(size, list)
                } else {
                    if (!path?.isEmpty()) {
                        when (currentStatus) {
                            Actions.GET_CALL_RECORD_BY_USER_STATUS_MAN -> {
                                BaseApplication.singleCallLastMp3Url = path
                            }
                            Actions.GET_CALL_RECORD_BY_USER_STATUS_TG -> {
                                BaseApplication.TgCallLastMp3Url = path

                            }
                            Actions.GET_CALL_RECORD_BY_USER_STATUS_TEMP_TG -> {
                                BaseApplication.TempTgCallLastMp3Url = path
                            }
                        }
                    }
                    isRequest = true
                }
            }
        })
    }

    var path = ""
    var currentStatus = 0
    private fun playVoice(size: Int?, list: List<VoiceInfo>?) {
//    private fun playVoice(size: Int?, list: List<UnknowVoiceInfo.Obj>?) {
        Log.e(TAG, "###$$$###>>>>>>>index:" + index)
        Log.e(TAG, "###$$$###>>>>>>>size:" + size)
        var index1 = index
        if (size!! > index1) {
            var obj = list!![index1]
            var fileName = obj?.TCRContent
            tcrId = obj?.id
            currentStatus = obj?.TCRSate!!
            path = RetrofitService.mp3Url + fileName
            Log.e(TAG, "=============>>>>>>>onNext>>>>path:" + path)
            Log.e(TAG, "=============>>>>>>>onNext>>>>index:" + index1)
            try {
                player?.reset()  //释放资源
                player?.setDataSource(path)
                player?.prepare()
                player?.start()
            } catch (e: IOException) {
                tcrId?.let {
                    infoReceived()
                }
                index++
                Log.e(TAG, "catch>>>>222222>>>index:" + index)
                if (index == size) {//播放完毕
                    index = 0
                    Log.e(TAG, "=============catch>>>>>>>onNext>>>>没有音频文件index:" + index1)
                }
                if (size == 0) {
                    Log.e(TAG, "=============>>>>>>>onNext>>>>没有音频文件size")
                    isRequest = true
                } else {
                    playVoice(size, list)
                }

            }
            index++
            Log.e(TAG, ">>>>222222>>>index:" + index)
            if (index == size) {//播放完毕
                Log.e(TAG, "=============>>>>>>>onNext>>>>没有音频文件index:" + index1)
            }
        }
    }

    override fun onDestroy() {//被销毁时发送广播，重启服务
        stopForeground(true)
        var intent = Intent("com.hat.receive.destroy")
        sendBroadcast(intent)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}