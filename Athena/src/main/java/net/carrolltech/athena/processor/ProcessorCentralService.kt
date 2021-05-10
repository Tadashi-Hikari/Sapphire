package net.carrolltech.athena.processor

import android.content.Intent
import android.os.IBinder
import edu.stanford.nlp.classify.ColumnDataClassifier
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File

class ProcessorCentralService: SapphireFrameworkService(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            Log.v("ProcessorCentralService started")
            when {
                // This is just a quick action for me, will be changed
                intent!!.action == "DELETE_CLASSIFIER" -> deleteClassifier()
                // This is just temporary
                else -> train()
            }
            return super.onStartCommand(intent, flags, startId)
        }catch (exception: Exception){
            Log.d("There was an intent error w/ the processor")
           exception.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun train(){
        Log.v("Passing to training")
        var intent = Intent().setClassName(this,"net.carrolltech.athena.processor.ProcessorTrainingService")
        startService(intent)
    }

    // This should be renamed, definitely
    fun process(intent: Intent?){
        var utterance = intent!!.getStringExtra(MESSAGE)
        var outgoingIntent = Intent()

        try{
            if(utterance != ""){
                Log.i("Loading the classifier")
                var classifier = loadClassifier()
                // This is specific to how CoreNLP works
                var datumToClassify = classifier.makeDatumFromLine("none\t${utterance}")
                // Can these two be combined, or done at the same time?
                var classifiedDatum = classifier.classOf(datumToClassify)
                var classifiedScores = classifier.scoresOf(datumToClassify)
                Log.v("Datum classification: ${classifiedDatum}")
                // This is an arbitrary number, and should probably be a configurable variable
                if(classifiedScores.getCount(classifiedDatum) >= .04){
                    Log.i("Text matches class ${classifiedDatum}")
                    // This could be an issue with the new design
                    outgoingIntent.putExtra(ROUTE,classifiedDatum)
                }else {
                    Log.i("Text does not match a class. Using default")
                    // This could be an issue with the new design
                    outgoingIntent.putExtra(ROUTE,"DEFAULT")
                }

                /**
                 * I actually may not need to send out unformatted text. This filter is transforming it,
                 * so the next module probably doesn't need the unformatted text. I can just log a reference
                 * for text & binary sources, so that if a module needs it then a request can be made for
                 * the base data along the pipeline. This prevents overcomplicating the protocol
                 */
                outgoingIntent.putExtra(MESSAGE,utterance)
                startService(outgoingIntent)
            }
        }catch(exception: Exception){
            Log.e("There was an error trying to process the text")
        }
    }

    fun loadClassifier(): ColumnDataClassifier{
        var classifierFile = File(filesDir,"intent.classifier")
        if(classifierFile.exists() != true) {
            var trainingIntent = Intent()
            // This should work...
            trainingIntent.setClassName(PACKAGE_NAME,"${PACKAGE_NAME}.ProcessorTrainingService")
            startService(trainingIntent)
        }
        return ColumnDataClassifier.getClassifier(classifierFile.canonicalPath)
    }

    fun deleteClassifier(){
        var file = File(filesDir,"Intent.classifier")
        file.delete()
    }
}