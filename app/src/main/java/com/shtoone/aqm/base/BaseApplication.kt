package com.shtoone.aqm.base

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDex
import android.text.TextUtils
import com.shtoone.aqm.network.RetrofitManager
import com.tencent.bugly.Bugly
import com.tencent.bugly.crashreport.CrashReport
import com.vondear.rxtools.RxLogTool
import com.vondear.rxtools.RxTool
import devliving.online.securedpreferencestore.RecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import org.litepal.LitePalApplication
import org.litepal.tablemanager.Connector
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Exception
import java.security.KeyStore

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
class BaseApplication : LitePalApplication() {
//class BaseApplication : Application() {

    companion object {
        /*******  后台配置  start   *******/
        /**
         * 手机连接ID
         */
        var id: String = ""
        /**
         * 视频时长（秒）
         */
        var videoTimeLength: Int = 0
        /**
         * GPS上传频率(秒)
         */
        var gpsInterval: Int = 0
        /**
         * 音频时长（秒）
         */
        var audioTimeLength: Int = 0
        /**
         * 图片大小（MB)
         */
        var pictureSize: Int = 0
        /**
         * 消息频率(秒)
         */
        var timerTime: Int = 0
        /**
         *
        消息开关（0：关 1：开 默认值：0）
         */
        var timerSwitch: Int = 0
        /**
         * GPS上传频率(m)
         */
        var gpsDistance: Int = 0
        /*******  后台配置  end     *******/

        var sn = "AQMNo4"//和username的数字一致
        var key = ""
        var userName = ""
        var latitude = ""//纬度，纬度取值最大是90
        var longitude = ""//经度，经度取值最大是180
        var address = ""//安全帽当前位置
        var prefStore: SecuredPreferenceStore? = null

        var singleCallLastMp3Url = ""
        var otherUserName = ""
        var otherRealName = ""
        var lastCallLastMp3Url = ""
        var TempTgCallLastMp3Url = ""
        var tempTeamName = ""
        var tempTeamCode = ""
        var TgCallLastMp3Url = ""
        var teamName = ""
        var teamCode = ""
        var noticeCallLastMp3Url = ""
        var trainCallLastMp3Url = ""


        var isOpenTimer = true
        var isOpenLocationService = false

    }

    override fun onCreate() {
        super.onCreate()

        RetrofitManager.getInstance()//初始化


        val context = applicationContext
        // 获取当前包名
        val packageName = context.packageName
        // 获取当前进程名
        val processName = getProcessName(android.os.Process.myPid())
        // 设置是否为上报进程
        val strategy = CrashReport.UserStrategy(context)
        strategy.isUploadProcess = processName == null || processName == packageName
        Bugly.init(getApplicationContext(), "f5dc34f646", false)
        //        Fabric.with(this, Crashlytics())

        RxTool.init(this)
        SecuredPreferenceStore.init(this, object : RecoveryHandler() {
            override fun recover(p0: Exception?, p1: KeyStore?, p2: MutableList<String>?, p3: SharedPreferences?): Boolean {
                return true
            }
        })
        prefStore = SecuredPreferenceStore.getSharedInstance()


        RxLogTool.init(this)

        var db = Connector.getDatabase()//获取数据库实例

    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private fun getProcessName(pid: Int): String? {

        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName = reader!!.readLine()
            if (!TextUtils.isEmpty(processName)) {
//                processName = processName.trim({ it <= ' ' })
                processName = processName.trim()
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                if (reader != null) {
                    reader!!.close()
                }
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}