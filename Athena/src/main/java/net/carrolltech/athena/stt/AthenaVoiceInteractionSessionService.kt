package net.carrolltech.athena.stt

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log

/**
 * This is a service that holds the information for a current session ("Hey Athena... do XYZ")
 */

class AthenaVoiceInteractionSessionService: VoiceInteractionSessionService(){
    override fun onNewSession(args: Bundle?): AthenaVoiceInteractionSession {
        Log.d(this.javaClass.name, "Creating a new session")
        return AthenaVoiceInteractionSession(this)
    }
}