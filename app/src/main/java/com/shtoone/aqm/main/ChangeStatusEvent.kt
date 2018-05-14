package com.shtoone.aqm.main

/**
 * Created by mzf on 2018/5/14.
 * Email:liangfeng093@gmail.com
 */
class ChangeStatusEvent {
    var tcrStatus = 0

    constructor(tcrStatus: Int) {
        this.tcrStatus = tcrStatus
    }
}