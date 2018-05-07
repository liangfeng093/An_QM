package com.shtoone.aqm.network

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
interface RetrofitService {

    companion object {

        val BaseURL: String = "http://114.55.108.58:8086/SSMS/"//新外网
        val mp3Url = BaseURL + "Callupload/"
    }

    /**
     * 登录
     * amqSN：安全帽编号
     */
    @GET("rest/app/SystemController/DeviceLogin/{amqSN}")
    fun loginHelmet(@Path("amqSN") amqSN: String): Observable<LoginBean>

}