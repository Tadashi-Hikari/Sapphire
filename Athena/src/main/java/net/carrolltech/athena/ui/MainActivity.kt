package net.carrolltech.athena.ui

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import net.carrolltech.athena.R
import net.carrolltech.athena.framework.SapphireUtils
import net.carrolltech.athena.stt_service.AthenaVoiceInteractionService
import org.w3c.dom.Text
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
        // Make sure that I don't need to do anything else here. I don't think I have a graceful shutdown if permissions are denied
        checkForPermissions()
        setContentView(R.layout.core_activity)
        var textView = findViewById<TextView>(R.id.textView)
        // For some reason this won't work when set in the XML
        textView.setHorizontallyScrolling(true)

        var assistIntent = Intent()
        // This isn't a thing anymore. And why was I even starting this here?
        assistIntent.setClassName(this,"com.example.sapphireassistantframework.voiceassistant.CoreVoiceInteractionService")
        // Is ths required to initialize the Android assistant?
        assistIntent.setAction(Intent.ACTION_ASSIST)
       // startService(assistIntent)
    }

    // This might not work. it may  have to go through core. But, it is started by an activity
    fun renameAssistant(view: View){
        var intent = Intent().setClassName(this,"net.carrolltech.athena.stt_service.AthenaVoiceInteractionService")
        intent.setAction("RENAME_SAPPHIRE")
        var name = findViewById<TextView>(R.id.assistant_name)
        intent.putExtra("NAME",name.text.toString())
        startService(intent)
    }

    fun launchSettings(view: View){
        var intent = Intent().setClassName(this,"net.carrolltech.athena.ui.SettingsActivity")
        startActivity(intent)
    }

    fun startAssistant(view: View){
        var intent = Intent().setAction(SapphireUtils().ACTION_SAPPHIRE_INITIALIZE)
        intent.setClassName(this,SapphireUtils().CORE_SERVICE)

        // This is to keep it compatible with Android 7.1
        if(Build.VERSION.SDK_INT >= 26) {
            startService(intent)
        }else{
            startService(intent)
        }
    }

    // What exactly is this supposed to be testing?
    fun testComponent(view: View){
        var testIntent = Intent().setAction("action.athena.TEST")
        testIntent.setClassName(this,SapphireUtils().MAIN_ACTIVITY)
        startActivity(testIntent)
        //startService(testIntent)
    }

    fun sendTestMessage(view: View){
        var editText= findViewById<EditText>(R.id.testInput)
        var testTextIntent = Intent().setAction("action.athena.TEST_INPUT")
        testTextIntent.setClassName(this,"net.carrolltech.athena.tts_service.SimpleTensorflowEngine")
        testTextIntent.putExtra("text",editText.text.toString())
        startService(testTextIntent)
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
            var intent = Intent().setAction(Settings.ACTION_APPLICATION_SETTINGS)
            startActivity(intent)
        }
    }
}