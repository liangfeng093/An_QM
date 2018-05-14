package com.shtoone.aqm.features.news

/**
 * post请求体
 * Created by mzf on 2018/5/14.
 * Email:liangfeng093@gmail.com
 */
class RequestNotReceive {
    var tcrfrom: String? = ""
    var tcrto: String? = null
    var tcrteamcode: String? = ""
    var tcrsate: String? = null
    var tcrjieshouzhuangtai: String? = null
    var tcrtime: String? = null
    var tcrphonejieshouzhuangtai: String? = null
    override fun toString(): String {
        return "RequestTalkHistory(tcrphonejieshouzhuangtai=$tcrphonejieshouzhuangtai,tcrfrom=$tcrfrom, tcrto=$tcrto, tcrteamcode=$tcrteamcode, tcrsate=$tcrsate, tcrjieshouzhuangtai=$tcrjieshouzhuangtai, tcrtime=$tcrtime)"
    }

}