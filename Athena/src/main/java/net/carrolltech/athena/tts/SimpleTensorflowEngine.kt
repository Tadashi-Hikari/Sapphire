package net.carrolltech.athena.tts

import android.content.Intent
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.tts.module.FastSpeech2
import net.carrolltech.athena.tts.module.MBMelGan
import net.carrolltech.athena.tts.TtsPlayer.AudioData
import net.carrolltech.athena.tts.utils.Processor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

class SimpleTensorflowEngine: SapphireFrameworkService(){

    var DEFAULT_INPUT_TEXT = "Unless you work on a ship, it's unlikely that you use the word boatswain in everyday conversation, so it's understandably a tricky one. The word - which refers to a petty officer in charge of hull maintenance is not pronounced boats-wain Rather, it's bo-sun to reflect the salty pronunciation of sailors, as The Free Dictionary explains./Blue opinion poll conducted for the National Post."
    lateinit var mFastSpeech2: FastSpeech2
    // This should be what turns text into phenoms
    lateinit var mMBMelGan: MBMelGan
    lateinit var mProcessor: Processor
    lateinit private var mTtsPlayer: TtsPlayer
    val SPEED = 1.0F
    lateinit var fastspeech: String
    lateinit var vocoder: String

    override fun onCreate() {
        super.onCreate()

        val FASTSPEECH2_MODULE = "fastspeech2_quant.tflite"
        val MELGAN_MODULE = "mbmelgan.tflite"
        fastspeech = copyFile(FASTSPEECH2_MODULE)
        vocoder = copyFile(MELGAN_MODULE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!!.action == "action.athena.TEST"){
            speak(DEFAULT_INPUT_TEXT)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun stopTts(){
        mTtsPlayer.interrupt()
    }

    fun speak(inputText:String){
        try {
            mFastSpeech2 = FastSpeech2(fastspeech)
            // This should be what turns text into phenoms
            mMBMelGan = MBMelGan(vocoder)
            mProcessor = Processor()
            mTtsPlayer = TtsPlayer()

            // This code is pretty much all from InputWorker
            val sentences = inputText.split("[.,]").toTypedArray()
            Log.d("speak: " + Arrays.toString(sentences))
            for (sentence in sentences) {
                val time = System.currentTimeMillis()
                val inputIds: IntArray = mProcessor.textToIds(sentence)
                val output: TensorBuffer = mFastSpeech2.getMelSpectrogram(inputIds, SPEED)
                val encoderTime = System.currentTimeMillis()
                val audioData: FloatArray = mMBMelGan.getAudio(output)
                val vocoderTime = System.currentTimeMillis()
                Log.d(
                    "Time cost: " + (encoderTime - time) + "+" + (vocoderTime - encoderTime) + "=" + (vocoderTime - time)
                )
                mTtsPlayer.play(AudioData(sentence, audioData))
            }
        }catch(exception: Exception){
            Log.d("Try again in a minute")
        }
    }

    // This is a longer, more secure version of what I do
    fun copyFile(strOutFileName: String): String {
        Log.d("start copy file $strOutFileName")
        val file = this.filesDir
        val tmpFile = file.absolutePath + "/" + strOutFileName
        val f = File(tmpFile)
        if (f.exists()) {
            Log.d("file exists $strOutFileName")
            return f.absolutePath
        }
        try {
            FileOutputStream(f).use { myOutput ->
                assets.open(strOutFileName).use { myInput ->
                    val buffer = ByteArray(1024)
                    var length = myInput.read(buffer)
                    while (length > 0) {
                        myOutput.write(buffer, 0, length)
                        length = myInput.read(buffer)
                    }
                    myOutput.flush()
                    Log.d("Copy task successful")
                }
            }
        } catch (e: Exception) {
            Log.e("copyFile: Failed to copy")
        } finally {
            Log.d("end copy file $strOutFileName")
        }
        return f.absolutePath
    }
}