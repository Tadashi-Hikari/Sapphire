package net.carrolltech.athena.stt_service

import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.os.TestLooperManager
import android.service.voice.VoiceInteractionSessionService
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import net.carrolltech.athena.framework.SapphireUtils
import org.json.JSONObject
import org.kaldi.*
import java.io.File

/**
 * This is a service that holds the information for a current session ("Hey Athena... do XYZ").
 * I can do whatever I want in this area, and also use the InteractionSession to handle the
 * app and user data. The other session is just for the hotword.
 *
 * Iirc this is required by Android to register the app as an assistant
 */

class AthenaVoiceInteractionSessionService: RecognitionListener, VoiceInteractionSessionService(){
    private lateinit var recognizer: CustomSpeechRecognizer

    val MESSAGE="assistant.framework.protocol.MESSAGE"

    // This will initialize the full recognizer in the background, waiting for the user to say "Athena"
    // This needs to be a second start point for CoreService
    override fun onCreate() {
        Log.v(
            this.javaClass.simpleName,
            "Starting up the SessionService. This is a heavyweight operation"
        )
        super.onCreate()
        System.loadLibrary("kaldi_jni");
        // This is what is initiating the STT
        setup()
    }

    // I have to take back the recognition speech for now, because it  interferes w/ listening
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var ttsIntent = Intent().setAction(SapphireUtils().ACTION_SAPPHIRE_SPEAK)
        // This says "What can  I do for you" after every time you say "Sapphire". It may be a bit extra
        ttsIntent.putExtra("SPEAKING_PAYLOAD", "What can I do for you")
        ttsIntent.setClassName(this,SapphireUtils().CORE_SERVICE)
        // This is an issue, because it overlaps w/ Athena talking
        //startService(ttsIntent)

        recognizer.startListening()
        return super.onStartCommand(intent, flags, startId)
    }

    // I don't need to start here, because it's started w/ onStartCommand
    override fun onNewSession(args: Bundle?): AthenaVoiceInteractionSession {
        Log.d(this.javaClass.name, "Creating a new session")
        return AthenaVoiceInteractionSession(this)
    }

    // This should run the very first time, right?
    // Is there a way to speed this up?
    fun setup(){
        var assets = Assets(this)
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        var model = Model(assetDir.toString()+"/model-android")

        recognizer = CustomSpeechRecognizer(model)
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
            var processIntent = Intent().setClassName(this,SapphireUtils().CORE_SERVICE)
            processIntent.putExtra(SapphireUtils().FROM,SapphireUtils().STT_ANDROID_SERVICE)
            processIntent.putExtra(MESSAGE,hypothesisJson.getString("text"))
            startService(processIntent)
        }
        // I am wondering if I should shut down the service here, or if Android will..
    }

    override fun onTimeout() {
    }

    override fun onDestroy() {
        super.onDestroy()

        recognizer.cancel()
        recognizer.shutdown()
    }
}