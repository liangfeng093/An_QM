package com.shtoone.aqm.features.bigfileupload

import org.litepal.crud.DataSupport

/**
 * Created by mzf on 2018/5/8.
 * Email:liangfeng093@gmail.com
 */
class ChunkInfo : DataSupport() {
    /**
     * 当前文件的分片值(当前文件是第几个)
     */
    var chunk = 0
    /**
     * 文件总分片值(切割文件的总个数)
     */
    var chunks = 0
    /**
     * 大文件的文件名 uuid
     */
    var fileName = ""
    /**
     * 大文件路径
     */
    var filePath = ""

    /**
     * 源文件名
     */
    var srcFileName = ""
    var uuid = ""
    /**
     * 分割时间
     */
    var cutTime = ""

    /**
     * 是否上传成功
     */
    var isUploadSuccess = false

    override fun toString(): String {
        return "ChunkInfo(uuid=$uuid,srcFileName=$srcFileName, chunk=$chunk, chunks=$chunks, fileName='$fileName', filePath='$filePath')"
    }


}