package net.carrolltech.athena.tts_service.tts

import android.content.Intent
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeechService
import android.util.Log
import net.carrolltech.athena.framework.SapphireUtils
import net.carrolltech.athena.tts_service.dispatcher.OnTtsStateListener
import net.carrolltech.athena.tts_service.dispatcher.TtsStateDispatcher

/**
 * This is the class required by Android to register the app as a Text to Speech service (including
 * the xml files)
 */

class AthenaTextToSpeechService: TextToSpeechService(){

    var speed = 1.0F

    // I think this is created as soon as it is set as the default TTS service
    override fun onCreate() {
        super.onCreate()

        // Do a permission check

        TtsManager.getInstance().init(this)

        TtsStateDispatcher.getInstance().addListener(object : OnTtsStateListener {
            override fun onTtsReady() {
                Log.d("TTS","The TTS is ready")
            }
            override fun onTtsStart(text: String) {}
            override fun onTtsStop() {}
        })
    }

    // This needs to be fully implemented
    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        TtsManager.getInstance().stopTts()
        return 1
    }

    // This also need to be fully implemented
    override fun onGetLanguage(): Array<String> {
        Log.d("TTS", "This isn't needed past API 18, where as I target #25")
        return arrayOf("English")
    }

    // This also needs to be fully implemented
    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        Log.d("TTS","For now, only english works")
        return 1
    }

    override fun onStop() {
        var intent = Intent().setAction("stop.tts")
        intent.setClassName(this,SapphireUtils().TTS_ACTUAL_SERVICE)
        startService(intent)
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        var intent = Intent().setAction("on.the.backend")
        intent.setClassName(this,SapphireUtils().TTS_ACTUAL_SERVICE)
        intent.putExtra("payload",request!!.charSequenceText.toString())
        startService(intent)
    }
}