package net.carrolltech.athena.tts.tts

import android.content.Intent
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.tts.dispatcher.OnTtsStateListener
import net.carrolltech.athena.tts.dispatcher.TtsStateDispatcher

class AthenaTTSLanding: SapphireFrameworkService() {
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