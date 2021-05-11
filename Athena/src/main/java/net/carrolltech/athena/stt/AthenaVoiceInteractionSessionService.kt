package net.carrolltech.athena.stt

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import org.json.JSONObject
import org.kaldi.*
import java.io.File

/**
 * This is a service that holds the information for a current session ("Hey Athena... do XYZ").
 * I can do whatever I want in this area, and also use the InteractionSession to handle the
 * app and user data
 */

class AthenaVoiceInteractionSessionService: RecognitionListener, VoiceInteractionSessionService(){

    private lateinit var recognizer: CustomSpeechRecognizer

    // This will initialize the full recognizer in the background, waiting for the user to say "Athena
    override fun onCreate() {
        Log.v(this.javaClass.simpleName,"Starting up the SessionService. This is a heavyweight operation")
        super.onCreate()
        System.loadLibrary("kaldi_jni");
        setup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        recognizer.startListening()
        Log.v(this.javaClass.simpleName,"I am listening, Lan")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNewSession(args: Bundle?): AthenaVoiceInteractionSession {
        Log.d(this.javaClass.name, "Creating a new session")
        recognizer.startListening()
        return AthenaVoiceInteractionSession(this)
    }

    fun setup(){
        var assets = Assets(this)
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        var model = Model(assetDir.toString()+"/model-android")

        recognizer = CustomSpeechRecognizer(model)
        Log.v(this.javaClass.simpleName,"Alright, I'm ready to go")
        recognizer.addListener(this)
        recognizer.startListening()
    }

    override fun onError(p0: Exception?) {
    }

    override fun onPartialResult(p0: String?) {
    }

    override fun onResult(hypothesis: String) {
        var hypothesisJson = JSONObject(hypothesis)
        recognizer.stop()
        Log.i(this.javaClass.name, "Result: ${hypothesisJson.getString("text")}")
        if(hypothesisJson.getString("text") != ""){
            var processIntent = Intent().setClassName(this,"${packageName}.processor.ProcessorCentralService")
            processIntent.putExtra("MESSAGE",hypothesisJson.getString("text"))
            startService(processIntent)
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