package net.carrolltech.athena.tts_service.tts

import android.content.Intent
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeechService
import android.util.Log
import net.carrolltech.athena.tts_service.dispatcher.OnTtsStateListener
import net.carrolltech.athena.tts_service.dispatcher.TtsStateDispatcher

class AthenaTextToSpeechService: TextToSpeechService(){

    var speed = 1.0F

    override fun onCreate() {
        super.onCreate()

        TtsManager.getInstance().init(this)

        TtsStateDispatcher.getInstance().addListener(object : OnTtsStateListener {
            override fun onTtsReady() {
                Log.d("TTS","The TTS is ready")
            }
            override fun onTtsStart(text: String) {}
            override fun onTtsStop() {}
        })
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        TtsManager.getInstance().stopTts()
        return 1
    }

    override fun onGetLanguage(): Array<String> {
        Log.d("TTS", "This isn't needed past API 18, where as I target #25")
        return arrayOf("English")
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        Log.d("TTS","For now, only english works")
        return 1
    }

    override fun onStop() {
        var intent = Intent().setAction("stop.tts")
        intent.setClassName(this,"net.carrolltech.athena.tts_service.tts.AthenaTTSLanding")
        startService(intent)
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        var intent = Intent().setAction("on.the.backend")
        intent.setClassName(this,"net.carrolltech.athena.tts_service.tts.AthenaTTSLanding")
        intent.putExtra("payload",request!!.charSequenceText.toString())
        startService(intent)
    }
}