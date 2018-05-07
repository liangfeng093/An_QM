package com.shtoone.aqm.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.shtoone.aqm.R
import com.shtoone.aqm.base.BaseApplication
import com.shtoone.aqm.features.login.LoginBean
import com.shtoone.aqm.features.login.LoginResponse
import com.shtoone.aqm.network.Observers
import com.shtoone.aqm.network.RetrofitManager
import com.vondear.rxtools.RxLogTool

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RetrofitManager.login(BaseApplication.sn, object : Observers.LoginObserver() {
            override fun onError(e: Throwable) {
                super.onError(e)
            }

            override fun onNext(t: LoginResponse) {
                var response = t?.obj as LoginBean
                RxLogTool.e("===>response:" + response)
            }
        })
    }
}
