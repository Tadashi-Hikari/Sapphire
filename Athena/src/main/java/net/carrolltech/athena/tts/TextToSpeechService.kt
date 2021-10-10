package net.carrolltech.athena.tts

import android.content.Intent
import net.carrolltech.athena.framework.SapphireFrameworkService
import org.tensorflow.lite.Interpreter
import java.io.File

class TextToSpeechService: SapphireFrameworkService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}