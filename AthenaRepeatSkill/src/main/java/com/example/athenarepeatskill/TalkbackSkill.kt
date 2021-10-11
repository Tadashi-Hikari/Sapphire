package com.example.athenarepeatskill

import android.app.Service
import android.content.Intent
import android.os.IBinder

/***
 * The TalkBack skill is just a fun way of showing how the Speech to Text and the Text to Speech
 * functions work, aside from the standard Android way (which is surprisingly confusing).
 * Using TTS is as simple as senting a string and an intent to the CoreService, while you can
 * use STT in the traditional way (through an onscreen activity) or through the custom implemented
 * service built into Athena.
 */

class TalkbackSkill: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        when(intent?.action){
            null -> recordTalkback()
            "ACTION_TALKBACK" -> sentToTalkback()
        }

        stopSelf()
        return null
    }

    fun initialize(){

    }

    fun recordTalkback(){
        // I can implement the traditional on screen recorder
        // or implement my own service only version
    }

    // This doesn't have to be here. It could hold a path straight to the TTS in Core
    fun sentToTalkback(){

    }
}