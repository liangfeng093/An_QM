package com.shtoone.aqm.network

import com.shtoone.aqm.features.bigfileupload.ChunkBody
import com.shtoone.aqm.features.login.LoginBean
import com.shtoone.aqm.features.login.LoginResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
interface RetrofitService {

    companion object {

        //        val BaseURL: String = "http://114.55.108.58:8086/SSMS/"//新外网
        val BaseURL: String = "http://192.168.0.158:8080/jeecg/"//luo
        val mp3Url = BaseURL + "Callupload/"
    }

    /**
     * 登录
     * amqSN：安全帽编号
     */
    @GET("rest/app/SystemController/DeviceLogin/{amqSN}")
    fun loginHelmet(@Path("amqSN") amqSN: String): Observable<LoginResponse>

    /**
     * 上传切割文件
     * SSMSTeamCallRecordController/videoMerge/upload/{key}
     */
    @Multipart
    @POST("/jeecg/rest/app/SystemController/videoMerge/upload/{amqSN}")
//    @POST("SSMSTeamCallRecordController/videoMerge/upload/{key}")
    fun uploadChunkFile(@Path("amqSN") key: String, @Part partList: List<MultipartBody.Part>): Observable<LoginResponse>
//    fun uploadChunkFile(body: ChunkBody): Observable<LoginResponse>

}