package net.carrolltech.athena.stt_service

import android.content.Intent
import android.service.voice.VoiceInteractionService
import android.util.Log
import net.carrolltech.athena.framework.SapphireUtils
import org.json.JSONObject
import org.kaldi.*
import java.io.File
import java.lang.Exception

/***
 * This code is ONLY SUPPOSED TO BE THE HOTWORD RECOGNIZER. Make sure it's AS LIGHTWEIGHT AS POSSIBLE
 */

class AthenaVoiceInteractionService: RecognitionListener, VoiceInteractionService(){
    private lateinit var recognizer: CustomSpeechRecognizer
    var name = "sapphire"

    // This class exists to let the system know that it is initialized
    override fun onReady() {
        super.onReady()
        System.loadLibrary("kaldi_jni");
        setup()
        Log.v(this.javaClass.simpleName,"Alright, I'm ready to go")
        var intent = Intent().setAction(SapphireUtils().ACTION_SAPPHIRE_SPEAK)
        intent.putExtra("SPEAKING_PAYLOAD","Hello there, I am Sapphire.")
        intent.setClassName(this, SapphireUtils().CORE_SERVICE)
        startService(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            // This is started by the system?
            "android.service.voice.VoiceInteractionService" -> Log.v(this.javaClass.name,intent.toString())
            "RENAME_SAPPHIRE" -> rename(intent)
            else -> Log.v(this.javaClass.name,"There was an error with the wake word starting intent")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is temporary, and needs to be put as a saved feature. It also should train from the user
    fun rename(intent: Intent){
        if(intent.hasExtra("NAME")){
            name = intent.getStringExtra("NAME")!!
            Log.i("athena","Renaming the assistant ${name}")
        }
    }

    fun setup(){
        var assets = Assets(this)
        // I could probably modify this to not be so archaic and not use such a rigid filestructure
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        var model = Model(assetDir.toString()+"/model-android")
        // This can be used to improve the processing, I think, since it's not a huge acustic model
        //recognizer = CustomSpeechRecognizer(model,"[\"athena\",\"hey athena\"]")
        recognizer = CustomSpeechRecognizer(model)
        recognizer.addListener(this)
        recognizer.startListening()
    }

    override fun onError(p0: Exception?) {
    }

    override fun onPartialResult(p0: String?) {
        // scan this for in between stuff
    }

    // This will pass a result while running, No need to start or stop the recognizer
    // Hmm, I don't actually think I am using this?
    override fun onResult(hypothesis: String) {
        var jsonUtterance = JSONObject(hypothesis)
        // Maybe I want to move this to onPartialResult, so it starts listening right away
        if(name.toLowerCase() in jsonUtterance.getString("text")) {
            // This sends it back to the Android service, so that it can properly work w/ the system
            var intent = Intent().setClassName(this,"${packageName}.stt_service.AthenaVoiceInteractionSessionService")
            startService(intent)
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