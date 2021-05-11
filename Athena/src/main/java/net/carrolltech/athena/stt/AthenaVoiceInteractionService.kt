package net.carrolltech.athena.stt

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import org.kaldi.*
import java.io.File
import java.lang.Exception

class AthenaVoiceInteractionService: RecognitionListener, VoiceInteractionService(){
    private lateinit var recognizer: CustomSpeechRecognizer

    override fun onReady() {
        super.onReady()
        System.loadLibrary("kaldi_jni");
        setup()
        Log.v(this.javaClass.name, "Ready to go")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            // This is started by the system?
            "android.service.voice.VoiceInteractionService" -> Log.v(this.javaClass.name,intent.toString())
            else -> Log.v(this.javaClass.name,"Something something something")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun setup(){
        var assets = Assets(this)
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        var model = Model(assetDir.toString()+"/model-android")
        //recognizer = CustomSpeechRecognizer(model,"[\"athena\",\"hey athena\"]")
        recognizer = CustomSpeechRecognizer(model)
        recognizer.addListener(this)
        recognizer.startListening()
    }

    override fun onError(p0: Exception?) {
    }

    override fun onPartialResult(p0: String?) {
        // scan this for in between stuff
    }

    // This will pass a result while running, No need to start or stop the recognizer
    override fun onResult(hypothesis: String) {
        var jsonUtterance = JSONObject(hypothesis)
        // Maybe I want to move this to onPartialResult, so it starts listening right away
        if("mega man" in jsonUtterance.getString("text")) {
            Log.v(this.javaClass.name, "What can I do for you Lan")
            var intent = Intent().setClassName(this,"${packageName}.stt.AthenaVoiceInteractionSessionService")
            startService(intent)
        }
    }

    override fun onTimeout() {
    }

    override fun onDestroy() {
        super.onDestroy()

        recognizer.cancel()
        recognizer.shutdown()
    }

}