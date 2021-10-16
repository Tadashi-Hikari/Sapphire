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
    private val DEFAULT_LANGUAGE = "eng"
    private val DEFAULT_COUNTRY = "USA"
    private val DEFAULT_VARIANT = "male,rms"


    private val mCountry = DEFAULT_COUNTRY
    private val mLanguage = DEFAULT_LANGUAGE
    private val mVariant = DEFAULT_VARIANT

    // I think this is created as soon as it is set as the default TTS service
    override fun onCreate() {
        super.onCreate()

        // Do a permission check
    }

    // This needs to be fully implemented
    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        return 1
    }

    // This also need to be fully implemented
    override fun onGetLanguage(): Array<String> {
        // This information isn't accurate. It's copied from Flite TTS just to prevent errors in Android
        return arrayOf(mLanguage,mCountry,mVariant)
    }

    // This also needs to be fully implemented
    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        // I believe 1 is true. It should be querying for the language, so I will need to fix this
        return 1
    }

    override fun onStop() {
        var intent = Intent().setAction("stop.tts")
        intent.setClassName(this,SapphireUtils().TTS_ACTUAL_SERVICE)
        startService(intent)
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        // This is just temporary, to prevent errors that may be generated in the system
        var requestedLanguage = request?.language
        var requestedCountry = request?.country
        var requestedVariant = request?.variant
        // I need to convert this int to a float that's compatible =
        var speechRate = request?.speechRate

        var intent = Intent().setAction("on.the.backend")
        intent.setClassName(this,SapphireUtils().TTS_ACTUAL_SERVICE)
        intent.putExtra("payload",request!!.charSequenceText.toString())
        startService(intent)
    }
}