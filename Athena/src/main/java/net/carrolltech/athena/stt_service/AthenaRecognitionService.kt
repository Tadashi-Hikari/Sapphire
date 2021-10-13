package net.carrolltech.athena.stt_service

import android.content.Intent
import android.speech.RecognitionService
import org.kaldi.*
import java.io.File

/**
 * The *vast* majority of this code is *hella* redundant....
 * I think this is required by Android to make this a standalone STT service, not just an assistant
 */

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
        setup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    fun setup(){
        var assets = Assets(this)
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        // These need to be moved out of setup, into their own thread
        var model = Model(assetDir.toString()+"/model-android")
        recognizer = CustomSpeechRecognizer(model)
        recognizer.addListener(this)
    }

    override fun onError(p0: Exception?) {
        // fix the error, or tell the user
    }

    override fun onPartialResult(p0: String?) {
        // scan this for in between stuff
    }

    // This will pass a result while running, No need to start or stop the recognizer
    override fun onResult(hypothesis: String) {
    }

    override fun onTimeout() {
    }

    override fun onDestroy() {
        super.onDestroy()

        recognizer.cancel()
        recognizer.shutdown()
    }
}