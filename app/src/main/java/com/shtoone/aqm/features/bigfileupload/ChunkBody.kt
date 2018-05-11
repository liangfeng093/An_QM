package com.shtoone.aqm.features.bigfileupload

/**
 * Created by mzf on 2018/5/11.
 * Email:liangfeng093@gmail.com
 */
class ChunkBody {
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
}