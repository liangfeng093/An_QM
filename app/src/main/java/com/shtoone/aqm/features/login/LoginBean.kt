package com.shtoone.aqm.features.login

/**
 * Created by mzf on 2018/5/7.
 * Email:liangfeng093@gmail.com
 */
class LoginBean {
    var username = ""
    var org_code = ""
    var TeamCode = ""
    var TeamName = ""
    var audioTimeLength = -1
    var gpsDistance = -1
    var gpsInterval = -1
    var pictureSize = -1
    var timerSwitch = -1
    var timerTime = -1
    var videoTimeLength = -1
    override fun toString(): String {
        return "LoginBean(username='$username', org_code='$org_code', TeamCode='$TeamCode', TeamName='$TeamName', audioTimeLength=$audioTimeLength, gpsDistance=$gpsDistance, gpsInterval=$gpsInterval, pictureSize=$pictureSize, timerSwitch=$timerSwitch, timerTime=$timerTime, videoTimeLength=$videoTimeLength)"
    }

}