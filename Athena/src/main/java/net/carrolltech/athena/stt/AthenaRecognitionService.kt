package net.carrolltech.athena.stt

import android.content.Intent
import android.speech.RecognitionService
import org.json.JSONObject
import org.kaldi.*
import java.io.File

class AthenaRecognitionService: RecognitionListener, RecognitionService(){

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        recognizer.startListening()
    }

    override fun onStopListening(listener: Callback?) {
        recognizer.stop()
    }

    override fun onCancel(listener: Callback?) {
        recognizer.cancel()
    }

    private lateinit var recognizer: CustomSpeechRecognizer

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("kaldi_jni");

        // This is going to make it run through the process twice. I need to offload the creation
        // I actually think I need to move setup to onBind()
        setup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
    // Maybe this should be a broadcast
    fun sendUtterance(utterance: String){
        var json = JSONObject(utterance)
        if("mega man" in json.getString("text")){
            var intent = Intent()
            intent.setClassName(this,"${this.packageName}.voiceassistant.CoreVoiceInteractionService")
            intent.setAction("android.speech.RecognitionService")
            //recognizer.cancel()
            //recognizer.shutdown()
            startService(intent)
            //onDestroy()

        }else if(json.getString("text") != "") {
            // This is a input module, it should always be sending to core. How would I wrap it?
            var coreServiceIntent: Intent = Intent()
        }
    }

    fun setup(){
        var startTime  = System.currentTimeMillis()
        var result = StringBuilder()

        var assets = Assets(this)
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        // These need to be moved out of setup, into their own thread
        var model = Model(assetDir.toString()+"/model-android")
        // This is the recognizer itself

        // See if I need to change this with a kaldi recognizer
        recognizer = CustomSpeechRecognizer(model)
        recognizer.addListener(this)
        var intent = Intent()
    }

    override fun onError(p0: Exception?) {
        // fix the error, or tell the user
    }

    // This will pass a result while running, No need to start or stop the recognizer
    // I can subclass this object as a hotword listener if I need to.
    override fun onPartialResult(p0: String?) {
        // scan this for in between stuff
    }

    // This will pass a result while running, No need to start or stop the recognizer
    override fun onResult(hypothesis: String) {
        sendUtterance(hypothesis)
    }

    override fun onTimeout() {
    }

    override fun onDestroy() {
        super.onDestroy()

        recognizer.cancel()
        recognizer.shutdown()
    }
}