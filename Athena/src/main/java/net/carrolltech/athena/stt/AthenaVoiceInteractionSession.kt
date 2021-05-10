package net.carrolltech.athena.stt

import android.content.Context
import android.service.voice.VoiceInteractionSession
import android.util.Log

class AthenaVoiceInteractionSession(context: Context): VoiceInteractionSession(context){
    override fun onCreate() {
        super.onCreate()
        Log.v(this.javaClass.name,"VoiceInteractionSession started")
    }

    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
    }
}