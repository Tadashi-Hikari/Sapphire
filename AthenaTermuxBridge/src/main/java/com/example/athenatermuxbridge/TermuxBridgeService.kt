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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        termuxStuff(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        when(intent?.action){
            "action.athena.skill.INITIALIZE" -> initialize()
        }

        return null
    }

    fun termuxStuff(intent: Intent?){
        // This is requried for a termux intent
        var one = "RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH"
        // For start flags, not stdin
        var two = "RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS"
    }

    // This is just a simple script that can be referenced to create an easier callback
    // There is also a PendingIntent callback that can be utilized directly from Termux
    fun generateSimpleScriptForTermux(){
        var script = "am 'this.service.class"
    }

    // Termux actually has native support for PendingIntent, so I just need to work from w/i there
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