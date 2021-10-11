package com.example.athenatermuxbridge

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlin.random.Random

/**
 * The Termux bridge is an ideal plugin to illustrate the use of a PendingIntent over a bound service.
 * Since things done in termux are likely not written with Android in mind, and can't really declare
 * a manifest that gives permissions, it allows the user to use all the permissions of the CoreService,
 * while having the flexibility of languages like Lisp or Python. Eventually, Athena will internalize
 * its own permission checks so that way this process can't be blatantly abused by bad actors.
 */

class TermuxBridgeService: Service(){

    override fun onBind(intent: Intent?): IBinder? {
        when(intent?.action){
            "action.athena.skill.INITIALIZE" -> initialize()
        }

        return null
    }

    fun sendPendingIntent(){
        Log.v(this.javaClass.name,"Termux bridge initializing")
        // Nothing special here, just a PendingIntent for this service
        val alarmIntent = Intent().setClassName(this.packageName,this.javaClass.canonicalName!!)
        val pendingIntent = PendingIntent.getService(this, Random.nextInt(),alarmIntent,0)
        // This is a placeholder extra string
        alarmIntent.putExtra("PENDING_INTENT",pendingIntent)
        startService(alarmIntent)
    }

    fun initialize(){
        // Yeah, I could have called this directly, but I might add things to initialize later
        sendPendingIntent()
    }
}