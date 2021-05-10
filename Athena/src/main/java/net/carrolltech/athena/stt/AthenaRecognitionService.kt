package net.carrolltech.athena.stt

import android.content.Intent
import android.speech.RecognitionService

class AthenaRecognitionService: RecognitionService(){
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        TODO("Not yet implemented")
    }

    override fun onStopListening(listener: Callback?) {

    }

    override fun onCancel(listener: Callback?) {
        TODO("Not yet implemented")
    }
}