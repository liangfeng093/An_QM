package com.shtoone.aqm.features.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.shtoone.aqm.base.BaseApplication
import com.shtoone.aqm.base.BaseObject
import com.shtoone.aqm.network.Observers
import com.shtoone.aqm.network.RetrofitManager
import com.vondear.rxtools.RxNetTool
import com.vondear.rxtools.RxTimeTool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by mzf on 2018/5/11.
 * Email:liangfeng093@gmail.com
 */
//class LocationService {
//}
class LocationService : Service() {

    val TAG = "LocationService"
    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, ">>>>>>>>>>>>>>onCreate")
        startLocation()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    var startLatlng: LatLng? = null
    var endLatlng: LatLng? = null
    var isFirst = true
    var tagDistance: Int? = null
    var isUpdate = true

    fun startLocation() {
        /**     初始化定位       **/
        launch {
            while (true) {
                isUpdate = true
                delay(5 * 1000)
            }
        }
        //声明AMapLocationClient类对象
        var mLocationClient: AMapLocationClient? = null
        //声明定位回调监听器
        var mLocationListener = object : AMapLocationListener {
            override fun onLocationChanged(p0: AMapLocation?) {
                BaseApplication.latitude = "" + p0?.latitude
                BaseApplication.longitude = "" + p0?.longitude
                if (isUpdate) {
                    isUpdate = false
                    if (RxNetTool.isNetworkAvailable(this@LocationService)) {
                        if (BaseApplication.latitude?.isNotEmpty() && BaseApplication.longitude?.isNotEmpty()) {
                            if (BaseApplication.latitude?.toFloat() > 0 && BaseApplication.longitude?.toFloat() > 0) {
                                var mills = RxTimeTool.getCurTimeMills()//时间戳
                                var time = RxTimeTool.milliseconds2String(mills)//时间
                                var body = UploadLocation()
                                body?.id = ""
                                body?.ugpusername = BaseApplication?.userName
                                body?.ugpaqmsn = BaseApplication.sn
                                body?.ugplng = p0?.longitude
                                body?.ugplat = p0?.latitude
                                body?.ugpdatetime = time
                                body?.ugpcontent = p0?.address
                                RetrofitManager.uploadLocation(body, object : Observers.LocationObserver() {
                                    override fun onNext(t: UploadLocationResponse) {
                                        Log.e(TAG, "定位上传成功")
                                        Log.e(TAG, "定位=====>t:" + t.toString())
                                    }
                                })
                            }
                        }
                    }
                }
                Log.e(TAG, "address:" + p0?.address)
                /*Log.e(TAG, "latitude:" + p0?.latitude)
                Log.e(TAG, "longitude:" + p0?.longitude)
                BaseApplication.latitude = "" + p0?.latitude
                BaseApplication.longitude = "" + p0?.longitude
                Log.e(TAG, "address:" + p0?.address)
                if (isFirst) {//第一次定位无需计算距离
                    isFirst = false
                    startLatlng = LatLng(p0?.latitude!!, p0?.longitude)
//                    startLatlng = LatLng(30.456149, 114.410889)
                } else {
                    endLatlng = LatLng(p0?.latitude!!, p0?.longitude)
//                    endLatlng = LatLng(30.456149, 110.310989)
                    //经度:114.410889
                    //纬度:30.456149
                    //计算两点之间的距离
                    var mills = RxTimeTool.getCurTimeMills()//时间戳
                    var time = RxTimeTool.milliseconds2String(mills)//时间
                    var distance = AMapUtils.calculateLineDistance(startLatlng, endLatlng)
//                    var uploadTime = BaseApplication.time
                    Log.e(TAG, "目标移动距离:" + distance)
                    var body = UploadLocation()
                    body?.id = ""
                    body?.ugpusername = BaseApplication.userName
//                    body?.ugpusername = BaseApplication.baseInfo?.userName
                    body?.ugpaqmsn = BaseApplication.sn
//                    body?.ugpaqmsn = BaseApplication.baseInfo?.helmetNum
                    body?.ugplng = p0?.longitude
                    body?.ugplat = p0?.latitude
                    body?.ugpdatetime = time
                    body?.ugpcontent = p0?.address
                    RetrofitManager.uploadLocation(body, object : Observers.LocationObserver() {
                        override fun onNext(t: UploadLocationResponse) {
                            Log.e(TAG, "定位上传成功")
                            Log.e(TAG, "=====>t:" + t.toString())
                        }
                    })

                }*/
            }

        }
        //初始化定位
        mLocationClient = AMapLocationClient(getApplicationContext())
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener)

        //声明AMapLocationClientOption对象
        var mLocationOption: AMapLocationClientOption? = null
        //初始化AMapLocationClientOption对象
        mLocationOption = AMapLocationClientOption()
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        /* mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
         if (null != mLocationClient) {
             mLocationClient.setLocationOption(mLocationOption);
             //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
             mLocationClient.stopLocation();
             mLocationClient.startLocation();
         }*/
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(300000);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(true);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption)
        //启动定位
        mLocationClient.startLocation()
    }

    /**
     * 日志拦截器
     */
    class HttpLogger : HttpLoggingInterceptor.Logger {
        override fun log(message: String?) {
            Log.e("HttpLogInfo", message)
        }

    }
}