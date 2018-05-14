package com.shtoone.aqm.interfaces

/**
 * Created by mzf on 2018/5/14.
 * Email:liangfeng093@gmail.com
 */
interface Actions {
    companion object {
        //通话状态（1个人 2班组 3临时班组）
        val SEND_CALL_STATUS_MAN = 1
        val SEND_CALL_STATUS_TG = 2
        val SEND_CALL_STATUS_TEMP_TG = 3

       /* val TCR_STATUS_MAN = "1"//个人
        val TCR_STATUS_TG = "2"//班组
        val TCR_STATUS_TEMP_TG = "3"//临时班组
        val TCR_STATUS_NOTICE = "4"
        val TCR_STATUS_TRAIN = "5"*/

        //切换频道
        val TCR_STATUS_MAN = 1
        val TCR_STATUS_TG = 2
        val TCR_STATUS_TEMP_TG = 3
        val TCR_STATUS_NOTICE = 4
        val TCR_STATUS_TRAIN = 5

        val MARK_MAN = 1
        val MARK_TEMP_TG = 1
        val MARK_TG = 0

        val GET_CALL_RECORD_BY_USER_STATUS_MAN = 1
        val GET_CALL_RECORD_BY_USER_STATUS_TG = 2
        val GET_CALL_RECORD_BY_USER_STATUS_TEMP_TG = 3

    }
}