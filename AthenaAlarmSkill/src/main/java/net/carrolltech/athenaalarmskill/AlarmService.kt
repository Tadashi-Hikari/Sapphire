package net.carrolltech.athenaalarmskill

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

    // This should be the entry point for the whole skill?
    override fun onBind(intent: Intent?): IBinder? {
        when(intent?.action) {
            "action.athena.skill.INITIALIZE" -> initialize()
            else -> setAlarm(intent)
        }

        // This isn't something that needs to run for a long time, so just break the binding when done
        stopSelf()
        return null
    }

    // I think this should have time to run, since it's all the same thread
    fun initialize(){
        Log.v("AlarmService","There is nothing this skill needs to do to initialize")
    }

    // I will need to error check this somewhere
    fun setAlarm(intent: Intent?){
        var alarmIntent = Intent().setAction(AlarmClock.ACTION_SET_ALARM)
        var hour = 0; var minutes = 0

        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour)
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes)
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, "This alarm was set by your assistant, Athena")
        startService(alarmIntent)

        // I can send this back to the TTS service, to notify me
        Log.v(this.javaClass.name,"Alarm set for ${hour}:${minutes}")
    }
}