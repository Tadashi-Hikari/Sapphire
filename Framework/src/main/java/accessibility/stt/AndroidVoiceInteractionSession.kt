package accessibility.stt

import android.content.Context
import android.service.voice.VoiceInteractionSession
import android.util.Log

/**
 * This interacts w/ the user and the app itself, using the Android designs.
 * The context is passed to it via whatever application called it
 */

class AndroidVoiceInteractionSession(context: Context): VoiceInteractionSession(context){
    override fun onCreate() {
        super.onCreate()
        Log.v(this.javaClass.name,"VoiceInteractionSession started")
    }

    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
    }
}