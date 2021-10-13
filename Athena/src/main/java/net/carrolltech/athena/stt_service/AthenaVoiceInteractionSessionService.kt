package net.carrolltech.athena.stt_service

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import net.carrolltech.athena.framework.SapphireUtils
import org.json.JSONObject
import org.kaldi.*
import java.io.File

/**
 * This is a service that holds the information for a current session ("Hey Athena... do XYZ").
 * I can do whatever I want in this area, and also use the InteractionSession to handle the
 * app and user data
 *
 * Iirc this is required by Android to register the app as an assistant
 */

class AthenaVoiceInteractionSessionService: RecognitionListener, VoiceInteractionSessionService(){
    private lateinit var recognizer: CustomSpeechRecognizer

    val MESSAGE="assistant.framework.protocol.MESSAGE"

    // This will initialize the full recognizer in the background, waiting for the user to say "Athena"
    // This needs to be a second start point for CoreService
    override fun onCreate() {
        Log.v(this.javaClass.simpleName,"Starting up the SessionService. This is a heavyweight operation")
        super.onCreate()
        System.loadLibrary("kaldi_jni");
        setup()
    }

    // I am pretty sure this is triggered by the system
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var ttsIntent = Intent().setAction(SapphireUtils().ACTION_SAPPHIRE_SPEAK)
        // I may have to personally retrain the tensorflow model, but I just can't do it on my laptop right now
        ttsIntent.putExtra("SPEAKING_PAYLOAD", "what can I do for you?")
        ttsIntent.setClassName(this,SapphireUtils().CORE_SERVICE)
        startService(ttsIntent)
        recognizer.startListening()
        return super.onStartCommand(intent, flags, startId)
    }

    // I don't need to start here, because it's started w/ onStartCommand
    override fun onNewSession(args: Bundle?): AthenaVoiceInteractionSession {
        Log.d(this.javaClass.name, "Creating a new session")
        return AthenaVoiceInteractionSession(this)
    }

    // This should run the very first time, right?
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
        if(hypothesisJson.getString("text").isNotBlank()){
            Log.i(this.javaClass.simpleName, "Result: ${hypothesisJson.getString("text")}")
            var processIntent = Intent().setClassName(this,SapphireUtils().PROCESSOR_SERVICE)
            processIntent.putExtra(MESSAGE,hypothesisJson.getString("text"))
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