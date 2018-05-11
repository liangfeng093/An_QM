package com.shtoone.aqm.main

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
import com.vondear.rxtools.RxZipTool
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.data
import android.app.Activity
import android.net.Uri
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.amap.api.mapcore.util.it
import com.shtoone.aqm.features.bigfileupload.ChunkBody
import com.shtoone.aqm.features.bigfileupload.ChunkInfo
import com.vondear.rxtools.RxFileTool
import org.litepal.crud.DataSupport
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
                    BaseApplication.username = it?.username
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
        customFile(filePath?.path, 5, tempPath)

        var firstChunkInfo = DataSupport.findFirst(ChunkInfo::class.java)
        Log.e(TAG, ">>>>>>>firstChunkInfo:" + firstChunkInfo)
        firstChunkInfo?.let {
            var chunkBody = ChunkBody()
            chunkBody?.chunk = it?.chunk!!
            chunkBody?.chunks = it?.chunks!!
            chunkBody?.fileName = it?.fileName!!
            RetrofitManager.uploadChunkFile(it?.filePath, chunkBody, Observers.LoginObserver())
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

                    var uuid = UUID.randomUUID()
                    var targetFileName = tempFilePath + "/" + uuid
//                    var targetFileName = tempFilePath + "/" + uuid + "-" + i
                    chunkInfo?.fileName = uuid?.toString()!!
                    chunkInfo?.filePath = targetFileName
                    chunkInfo?.chunk = i?.toInt()
                    chunkInfo?.chunks = chunks?.toInt()
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
