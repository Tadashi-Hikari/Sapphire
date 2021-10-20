package net.carrolltech.athenaalarmskill

import android.app.PendingIntent
import android.app.PendingIntent.getService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.AlarmClock
import android.util.Log

/***
 * The Alarm Skill is probably the best simple example of how the NLP processor works, as it takes
 * variables, and changes how it acts depending on the utterance of the user. You will see that the
 * package has .intent and .entity files as assets which are used to train the processor right off
 * the bat. Further modifications to this can be done but will either be saved in a public directory
 * so they can be modified by a plain text editor, or in the CoreServices private space (which it
 * can be used to pass the information to text editors using more fine grained permissions). It is
 * also a pretty essential feature for a virtual assistant. Since this is also one of the most
 * complex skills included, it illustrates a more in-dept registration for the CoreService
 */

class AlarmService: Service(){

    // This ID is what will be used by CoreService to reference the pending intent. It should come from core service
    val ID = 24

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "action.athena.skill.INITIALIZE" -> initialize()
            else -> (intent)
        }

       return super.onStartCommand(intent, flags, startId)
    }

    // This should be the entry point for the whole skill?
    override fun onBind(intent: Intent?): IBinder? {
        // This doesn't interfere w/ the Devs useage of Bind. but i can be opted out of with an 'if'. Will that cause issues?
        var thisIntent = Intent().setClassName(this,"net.carrolltech.athenaalarmskill.AlarmService")
        // This should have the ability to update, since Core will add new data. I think this works right....
        var pendingIntent = getService(this,ID,thisIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        var returnIntent = Intent().setClassName(this,"net.carrolltech.athena.core.CoreService")
        returnIntent.putExtra("PendingIntent",pendingIntent)
        startService(returnIntent)
        return null
    }

    // I think this should have time to run, since it's all the same thread
    fun initialize(){
        Log.v("AlarmService","There is nothing this skill needs to do to initialize")
        // This should have a way to dynamically add files, if there are non in assets
    }

    // I will need to error check this somewhere
    fun createAlarm(intent: Intent?){
        var alarmIntent = Intent().setAction(AlarmClock.ACTION_SET_ALARM)
        // Hour, minute, & seconds fall under time... How do i parse them?
        // I could use SUTime
        // timedate(Hour, minute, seconds?) (timer?)(yes,no), message, day-of-week, repeat,
        var hour = 0; var minutes = 0

        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour)
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes)
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, "This alarm was set by your assistant, Athena")
       //startService(alarmIntent)

        // I can send this back to the TTS service, to notify me
        Log.v(this.javaClass.name,"Alarm set for ${hour}:${minutes}")
        //speak(time)
        var speakIntent = Intent().setClassName(this,"net.carrolltech.athena.CoreService")
        speakIntent.putExtra("SPEAKING_PAYLOAD","Alright, an alarm has been set for you")
        startService(speakIntent)
    }

    //fun readAlarm(intent: Intent){}
    //fun updateAlarm(intent: Intent){}
    //fun deleteAlarm(intent: Intent){}
}