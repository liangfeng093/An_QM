package com.shtoone.aqm.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.shtoone.aqm.R
import com.shtoone.aqm.base.BaseApplication
import com.shtoone.aqm.features.login.LoginBean
import com.shtoone.aqm.features.login.LoginResponse
import com.shtoone.aqm.network.Observers
import com.shtoone.aqm.network.RetrofitManager
import com.vondear.rxtools.RxLogTool
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.amap.api.mapcore.util.fi
import com.amap.api.mapcore.util.it
import com.shtoone.aqm.base.BaseObject
import com.shtoone.aqm.features.bigfileupload.ChunkBody
import com.shtoone.aqm.features.bigfileupload.ChunkInfo
import com.shtoone.aqm.features.bigfileupload.UploadChunkFileResponse
import com.shtoone.aqm.features.location.LocationService
import com.vondear.rxtools.RxFileTool
import kotlinx.coroutines.experimental.launch
import org.litepal.crud.DataSupport
import org.litepal.crud.DataSupport.findFirst
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {
    val VIDEO_WITH_CAMERA = 10001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RetrofitManager.login(BaseApplication.sn, object : Observers.LoginObserver() {
            override fun onError(e: Throwable) {
                super.onError(e)
            }

            override fun onNext(t: LoginResponse) {
                var response = t?.obj as LoginBean
                response?.let {
                    BaseApplication.userName = it?.username
                }
                RxLogTool.e("===>response:" + response)
            }
        })


        btn_shock?.setOnClickListener {
            var vibrator = this@MainActivity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(5 * 1000)
        }


        btn_video?.setOnClickListener {
            var tempPath = filesDir?.path + "/" + resources?.getString(R.string.file_temp)

            if (!RxFileTool.fileExists(tempPath)) {
                RxFileTool.initDirectory(tempPath)
            }
            var uri = Uri.fromFile(File(tempPath))//设置视频录制保存地址的uri
            var intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            //设置视频录制的最长时间
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)//设置视频录制保存地址的uri
            //设置视频录制的画质
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            startActivityForResult(intent, VIDEO_WITH_CAMERA);
        }
//        RxZipTool.fileToZip(resources)
        var path = "" + RxFileTool.getRootPath() + "/DCIM/Camera/"
        var file = File(path)
        Log.e(TAG, ">>>>>>>path:" + path)
        Log.e(TAG, ">>>>>>>file1:" + file?.length())
        Log.e(TAG, ">>>>>>>file2:" + file?.list())
        file?.list()?.forEach {
            Log.e(TAG, ">>>>>>>file3:" + it)
        }

        var tempPath = filesDir?.path + "/" + resources?.getString(R.string.file_temp)
        var filePath = File(path + "VID_20180509_143926.3gp")

        var chunkInfos = DataSupport.where("srcFileName = ?", "VID_20180509_143926.3gp")
                .order("chunk asc")
                .find(ChunkInfo::class.java)

        if (chunkInfos?.size!! == 0) {
            customFile(filePath?.path, 5, tempPath)
        } else {
            Log.e(TAG, ">>>>>>>文件已切割:")
        }

        Log.e(TAG, ">>>>>>>chunkInfos:" + chunkInfos)
        if (chunkInfos?.size!! > 0) {
            var index = 0
            var isFinish = true
            launch {
                while (true) {
                    if (index == chunkInfos?.size) {
                        break
                    }
                    if (isFinish) {
                        isFinish = false
                        var chunkBody = ChunkBody()
                        var it = chunkInfos?.get(index)
                        chunkBody?.chunk = it?.chunk!!
                        chunkBody?.chunks = it?.chunks!!
                        chunkBody?.uuid = it?.uuid
//                        chunkBody?.fileName = it?.fileName!!
                        RetrofitManager.uploadChunkFile(it?.filePath, chunkBody, object : Observers.UploadChunkFileObserver() {
                            override fun onComplete() {
                                Log.e(TAG, ">>>>>>>uploadChunkFile_onComplete:")
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                Log.e(TAG, ">>>>>>>uploadChunkFile---e:" + e)
                            }

                            override fun onNext(t: UploadChunkFileResponse) {
//                        override fun onNext(t: BaseObject<String>) {
                                Log.e(TAG, ">>>>>>>uploadChunkFilet:" + t)
                                if ("5"?.equals(t?.success)) {
                                    isFinish = true
                                    index++
                                }
                            }
                        })
                    }
                }
            }

        }
        /*firstChunkInfos?.let {
            var chunkBody = ChunkBody()
            chunkBody?.chunk = it?.chunk!!
            chunkBody?.chunks = it?.chunks!!
            chunkBody?.fileName = it?.fileName!!
            RetrofitManager.uploadChunkFile(it?.filePath, chunkBody, Observers.LoginObserver())
        }*/

        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)//自定义的code
        } else {
            var intent = Intent(this@MainActivity, LocationService::class.java)
//            startService(intent)
        }

    }

    val LOCATION_REQUEST_CODE = 1001
    val RECORD_AUDIO_REQUEST_CODE = 1002


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                Log.e(TAG, "onRequestPermissionsResult")
                var intent = Intent(this@MainActivity, LocationService::class.java)
//                startService(intent)
            }
            RECORD_AUDIO_REQUEST_CODE -> {
                Toast.makeText(this, "拿到录音权限", Toast.LENGTH_LONG).show()
                Log.e(TAG, ">>>>>>>拿到录音权限:")
            }
        }
    }

    /**
     * 当前文件的分片值(当前文件是第几个)
     */
    var chunk = 0
    /**
     * 文件总分片值(切割文件的总个数)
     */
    var chunks = 0
    /**
     * 文件分片大小1M
     */
    var chunkLength = 1024 * 1024 * 1

    /**
     * 分割文件
     */
    fun customFile(srcPath: String, chunkSize: Int, targetFolderPath: String) {
//    fun customFile(srcPath: String, chunkSize: Int, targetFolderPath: String): ChunkInfo? {
//    fun customFile(chunkInfo: ChunkInfo) {
        /*if (srcPath?.isEmpty() || targetFolderPath?.isEmpty()) {
            return null
        }*/
        var tempPath = filesDir?.path + "/" + resources?.getString(R.string.file_temp)
        if (!RxFileTool.fileExists(tempPath)) {//文件夹不存在
            RxFileTool.initDirectory(tempPath)
        }
        var srcFile = File(srcPath)//源文件
        var srcSize = srcFile?.length()//源文件大小
        var targetFileSize = chunkLength * chunkSize//分片文件大小
        var chunks = srcSize / targetFileSize
        var lastSize = 0L
        if (srcSize % targetFileSize != 0L) {
            lastSize = srcSize - (chunks * targetFileSize)
            chunks = chunks + 1
        }
        //生成缓存文件jia
        var tempFilePath = tempPath + "/" + srcFile?.name?.substring(0, srcFile?.name?.length!! - 4)
//        var tempFilePath = tempPath + "/" + chunkInfo?.fileName
        Log.e(TAG, ">>>>>>>tempFilePath:" + tempFilePath)
        if (RxFileTool.initDirectory(tempFilePath)) {
            var uuid = UUID.randomUUID()
            var inputStream: InputStream? = null//输入字节流
            var bis: BufferedInputStream? = null//输入缓冲流
            var bytes = ByteArray(1024)////每次读取文件的大小为1MB
//            var len = -1 //每次读取的长度值
            var len = 0 //每次读取的长度值
            try {
                inputStream = FileInputStream(srcFile)
                bis = BufferedInputStream(inputStream)
                Log.e(TAG, ">>>>>>>inputStream:" + inputStream)
                Log.e(TAG, ">>>>>>>bis:" + bis)
                for (i in 1..chunks) {//1 到 chunks
//                for (i in 0 until chunks) {//0 到 chunks
                    Log.e(TAG, ">>>>>>>i:" + i)
                    var chunkInfo = ChunkInfo()

//                    var targetFileName = tempFilePath + "/" + uuid
                    var targetFileName = tempFilePath + "/" + srcFile?.name + "-" + i
                    chunkInfo?.fileName = srcFile?.name + "-" + i
                    chunkInfo?.uuid = uuid?.toString()!!
                    chunkInfo?.filePath = targetFileName
                    chunkInfo?.chunk = i?.toInt()
                    chunkInfo?.chunks = chunks?.toInt()
                    chunkInfo?.srcFileName = srcFile?.name
                    chunkInfo?.save()//保存到数据库
                    Log.e(TAG, ">>>>>>>customFile_chunkInfo:" + "(" + i + ")" + chunkInfo)
//                    Log.e(TAG, ">>>>>>>chunkInfo:" + "(" + "" + "" + ")" + chunkInfo)
//                    Log.e(TAG, ">>>>>>>targetFileName:" + targetFileName)
                    var fos = FileOutputStream(targetFileName)
                    var bos = BufferedOutputStream(fos)
                    var count = 0
                    while (len != -1) {
//                    while ((len = bis?.read(bytes)) != 0) {
                        len = bis?.read(bytes)//输入流读取
                        bos?.write(bytes, 0, len)
                        count += len

                        if (i != chunks && count >= targetFileSize) {
                            break
                        } else if (i == chunks && count >= lastSize) {
                            break
                        }
                    }
                    bos.flush()//刷新
                    bos.close()
                    fos.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, ">>>>>>>EEEEEEE1:" + e)
                e?.stackTrace?.forEach {
                    Log.e(TAG, ">>>>>>>EEEEEEE2:" + it)
                }
            } finally {//关流
                inputStream?.close()
//                if (inputStream != null) inputStream?.close()
                bis?.close()
//                if (bis != null) bis?.close()
            }

        }
        Log.e(TAG, ">>>>>>>文件切割完毕:")
//        chunkInfo?.save()//保存到数据库
//        return chunkInfo
    }


/*
    fun writeTo(file: File, chunkInfo: ChunkInfo) {
        //创建随机流
        var randomAccessFile = RandomAccessFile(file, "r")
        //1KB缓冲区读取数据
        var size = 1024 * 1
        var tmp = ByteArray(size)
        //从指定位置读取流
        randomAccessFile?.seek((chunk * chunkLength).toLong())
        randomAccessFile?.read(tmp, 0, 1024)//读出字节数组读出有效个字节个数(n)
        var n = 0
        var readLength = 0L//已读字节数(长度)
        while (readLength <= chunkLength?.toLong() - 1024) {
            n = randomAccessFile?.read(tmp, 0, 1024)//读出字节数组读出有效个字节个数(n)
            readLength += 1024
            //写入输出流
            out?.write(tmp, 0, n)
        }
    }*/

    val TAG = this.javaClass.name

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode === Activity.RESULT_OK && requestCode === VIDEO_WITH_CAMERA) {
                val uri = data?.getData()
//                uri?.
                Log.e(TAG, "onActivityResult1: " + uri.toString())
                Log.e(TAG, "onActivityResult2: " + uri?.path)
                Log.e(TAG, "onActivityResult3: " + RxFileTool.getRootPath() + uri?.path)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}
