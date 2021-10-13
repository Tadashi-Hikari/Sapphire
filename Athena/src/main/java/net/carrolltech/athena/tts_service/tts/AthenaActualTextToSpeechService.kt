package net.carrolltech.athena.tts_service.tts

import android.content.Intent
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.tts_service.dispatcher.OnTtsStateListener
import net.carrolltech.athena.tts_service.dispatcher.TtsStateDispatcher

/**
 * This section is where I am actually doing the audio processing. The AthenaTextToSpeechService is the
 * service that is required by Android to use TTS, so
 */

class AthenaActualTextToSpeechService: SapphireFrameworkService() {
    var speed = 1.0F
    var ready = false
    var queue = ""

    override fun onCreate() {
        super.onCreate()
        TtsManager.getInstance().init(this)

        TtsStateDispatcher.getInstance().addListener(object : OnTtsStateListener {
            override fun onTtsReady() {
                Log.d("The TTS is ready")
                ready = true
                TtsManager.getInstance().speak(queue, speed, true)
            }
            override fun onTtsStart(text: String) {
            }
            override fun onTtsStop() {
                Log.d("The TTS has stopped")
            }
        })


    }

    // This is where the literal audio processing happens. This is core code
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!!.action == "on.the.backend") {
            if(ready == true) {
                var inputText = intent.getStringExtra("payload")
                TtsManager.getInstance().speak(inputText, speed, true)
            }else{
                queue = intent.getStringExtra("payload")!!
            }
        }else if(intent!!.action == "stop.tts"){
            TtsManager.getInstance().stopTts()
        }

        return super.onStartCommand(intent, flags, startId)
    }
}