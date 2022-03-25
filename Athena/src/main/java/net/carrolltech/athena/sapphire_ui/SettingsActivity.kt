package net.carrolltech.athena.sapphire_ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import net.carrolltech.athena.R

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun sendUserToAssistSelect(view: View){
        // For some reason, this sends you to the digital assistant
        var intent = Intent().setAction(Settings.ACTION_VOICE_INPUT_SETTINGS)
        startActivity(intent)
    }

    fun sendUserToVoiceSelect(view: View){
        var intent = Intent().setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}