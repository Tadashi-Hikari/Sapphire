package com.example.sapphire.assistant

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * This creates a foreground service, that simply does all
 * of the listening for the users hotword, etc
 */

class SimpleAssistantService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}