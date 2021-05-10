package net.carrolltech.athena.ui

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.PackageManager
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import net.carrolltech.athena.R
import android.os.Bundle as Bundle


class MainActivity: Activity()
{
    inner class assistantActivity: VoiceInteractionSession(this){

    }
    // This needs to be loaded from a config table
    private var tables = listOf("registration.tbl","defaultmodules.tbl","background.tbl","routetable.tbl","alias.tbl")
    val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"
    val MESSAGE="assistant.framework.protocol.MESSAGE"
    lateinit var coreBroadcastReceiver: BroadcastReceiver
    lateinit var coreDirectoryPicker: Service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //checkForPermissions()
        setContentView(R.layout.core_activity)
        var textView = findViewById<TextView>(R.id.textView)
        // For some reason this won't work when set in the XML
        textView.setHorizontallyScrolling(true)

        var assistIntent = Intent()
        assistIntent.setClassName(this,"com.example.sapphireassistantframework.voiceassistant.CoreVoiceInteractionService")
        assistIntent.setAction(Intent.ACTION_ASSIST)
       // startService(assistIntent)
    }

    fun startAssistant(view: View){
        var intent = Intent().setAction("assistant.framework.processor.action.INITIALIZE")
        intent.setClassName(this,"net.carrolltech.athena.core.CoreService")
        startService(intent)
    }

    // This will likely need to be more dynamic. This is just checking for permissions
    fun checkForPermissions(){
        when{
            ContextCompat.checkSelfPermission(
                this,
                "android.permission.RECORD_AUDIO"
            ) == PackageManager.PERMISSION_DENIED -> {
                requestPermissions(
                    arrayOf("android.permission.RECORD_AUDIO"),
                    PackageManager.PERMISSION_GRANTED
                )
            }
        }
    }


    // Gracefully handle denied permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if((grantResults.isNotEmpty())){
            Log.v("CoreCentralActivity","Permission granted")
        }else{
            Log.e("CoreCentralActivity","Permission must be granted for use")
        }
    }
}