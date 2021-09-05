package com.example.genericskill

import android.app.Service
import android.content.Intent
import android.os.IBinder

class GenericBoundService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        startService()
        return null
    }

    fun startService(){
        if(null == "register/install"){

        }else{
            doTheThing()
        }
    }

    fun doTheThing(){

    }
}