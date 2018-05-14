package com.shtoone.aqm.network

import com.shtoone.aqm.base.BaseObject
import com.shtoone.aqm.features.bigfileupload.UploadChunkFileResponse
import com.shtoone.aqm.features.location.UploadLocation
import com.shtoone.aqm.features.location.UploadLocationResponse
import com.shtoone.aqm.features.login.LoginResponse
import com.shtoone.aqm.features.news.RequestNotReceive
import com.shtoone.aqm.features.news.UnknowVoiceInfo
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
interface RetrofitService {

    companion object {

        val BaseURL: String = "http://114.55.108.58:8086/SSMS/"//新外网
        //        val BaseURL: String = "http://192.168.0.158:8080/jeecg/"//luo
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
    fun uploadChunkFile(@Path("amqSN") key: String, @Part partList: List<MultipartBody.Part>): Observable<UploadChunkFileResponse>
//    fun uploadChunkFile(@Path("amqSN") key: String, @Part partList: List<MultipartBody.Part>): Observable<BaseObject<String>>
//    fun uploadChunkFile(body: ChunkBody): Observable<LoginResponse>


    /**
     * 上传定位信息
     */
    @POST("rest/app/SystemController/Personnelpositioningaddorupdata")
    fun uploadLocation(@Body body: UploadLocation): Observable<UploadLocationResponse>

    /**
     *  小幺鸡:聊天窗口记录查询
     *  1.获取通知或培训(未接收)
     */
//    @POST("rest/SSMSTeamCallRecordController/sendCallToUserOnTalk/{key}.json")
//    fun getNotReceive(@Path("key") key: String, @Body body: RequestNotReceive): Observable<FindCallRecordsResponseBean>

    /**
     * 获取当前帽子未接收消息的列表
     */
    @GET("rest/app/SystemController/getCallRecordByUser")
    fun getVoiceInfo(@Query("username") username: String): Observable<UnknowVoiceInfo>

    /**
     * 语音通知已播放
     */
    @GET("rest/app/SystemController/updateCall")
    fun infoReceived(@Query("tcrId") tcrId: String): Observable<ResponseBody>

}