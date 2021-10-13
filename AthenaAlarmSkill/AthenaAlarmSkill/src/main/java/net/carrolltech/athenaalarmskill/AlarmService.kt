package net.carrolltech.athenaalarmskill

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.AlarmClock
import android.util.Log
import kotlin.random.Random

class AlarmService: Service(){

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "action.athena.skill.INITIALIZE" -> initialize()
            else -> setAlarm(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is kind of a generic thing that needs to be in all skills. Change for your skill data
    fun initialize(){
        Log.v(this.javaClass.name,"Alarm skill initializing")
        // Nothing special here, just a PendingIntent for this service
        var alarmIntent = Intent().setClassName(this.packageName,this.javaClass.canonicalName)
        var pendingIntent = PendingIntent.getService(this, Random.nextInt(),alarmIntent,0)
        // This is a placeholder extra string
        alarmIntent.putExtra("PENDING_INTENT",pendingIntent)
        startService(alarmIntent)
    }

    // I will need to error check this somewhere
    fun setAlarm(intent: Intent?){
        Log.d("AlarmService","Setting alarm (Demo)")
        var alarmIntent = Intent().setAction(AlarmClock.ACTION_SET_ALARM)
        var hour = 5; var minutes = 0

        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour)
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes)
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, "This alarm was set by your assistant, Athena")
        startService(alarmIntent)
        Log.v(this.javaClass.name,"Alarm set for ${hour}:${minutes}")
    }

    // This is used really just to start the service from a dead state
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}