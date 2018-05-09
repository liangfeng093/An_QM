package com.shtoone.aqm.features.bigfileupload

import java.io.File
import java.io.OutputStream
import java.io.RandomAccessFile

/**
 * Created by mzf on 2018/5/8.
 * Email:liangfeng093@gmail.com
 */
class CustomFileBody {
    var file: File? = null
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

    constructor(file: File?) {
        this.file = file
    }

    constructor(chunkInfo: ChunkInfo) {
//        CustomFileBody(File(chunkInfo?.filePath))
        chunk = chunkInfo?.chunk
        chunks = chunkInfo?.chunks
        file = File(chunkInfo?.filePath)
        if (chunk == chunks) {

        }
    }

    fun writeTo(out: OutputStream) {
        if (out == null) {
            throw  IllegalArgumentException("Output stream may not be null")
        } else {
            //创建随机流
            var randomAccessFile = RandomAccessFile(file, "r")
            //1KB缓冲区读取数据
            var size = 1024 * 1
            var tmp = ByteArray(size)
            try {
                if (chunk + 1 < chunks) {//中间分片(不是第一个)
                    //从指定位置读取流
                    randomAccessFile?.seek((chunk * chunkLength).toLong())
                    var n = 0
                    var readLength = 0L//已读字节数(长度)
                    while (readLength <= chunkLength?.toLong() - 1024) {
                        n = randomAccessFile?.read(tmp, 0, 1024)//读出字节数组读出有效个字节个数(n)
                        readLength += 1024
                        //写入输出流
                        out?.write(tmp, 0, n)
                    }
                    if (readLength <= chunkLength) {
                        n = randomAccessFile.read(tmp, 0, (chunkLength - readLength)?.toInt());
                        out.write(tmp, 0, n);
                    }
                } else {
                    randomAccessFile.seek((chunk * chunkLength).toLong())
                    var n = 0
                    while (n != -1) {
                        n = randomAccessFile.read(tmp, 0, 1024)
                        out.write(tmp, 0, n)
                    }
                }
                out?.flush()//缓冲区数据写入输出流
            } finally {
                randomAccessFile?.close()
            }
        }
    }
}