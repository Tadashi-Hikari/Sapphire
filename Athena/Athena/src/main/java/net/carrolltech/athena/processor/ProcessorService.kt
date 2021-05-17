package net.carrolltech.athena.processor

import android.content.Intent
import android.os.IBinder
import edu.stanford.nlp.classify.ColumnDataClassifier
import net.carrolltech.athena.R
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File

class ProcessorService: SapphireFrameworkService(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            Log.v("ProcessorCentralService started")
            when {
                else -> process(intent)
            }
        }catch (exception: Exception){
            Log.d("There was an intent error w/ the processor")
           exception.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This should be renamed, definitely
    fun process(intent: Intent?){
        var utterance = intent!!.getStringExtra(MESSAGE)
        var outgoingIntent = Intent()

        try{
            if(utterance == "delete") {
                deleteClassifier()
            }else if(utterance != ""){
                Log.i("Loading the classifier")
                var classifier = loadClassifier()
                if(classifier != null) {
                    // This is specific to how CoreNLP works
                    var datumToClassify = classifier.makeDatumFromLine("none\t${utterance}")
                    // Can these two be combined, or done at the same time?
                    var classifiedDatum = classifier.classOf(datumToClassify)
                    var classifiedScores = classifier.scoresOf(datumToClassify)
                    Log.v("Datum classification: ${classifiedDatum}")
                    // This is an arbitrary number, and should probably be a configurable variable
                    if (classifiedScores.getCount(classifiedDatum) >= .04) {
                        Log.i("Text matches class ${classifiedDatum}")
                        outgoingIntent.putExtra(ROUTE, classifiedDatum)
                    } else {
                        Log.i("Text does not match a class. Using default")
                        outgoingIntent.putExtra(ROUTE, "DEFAULT")
                    }

                    outgoingIntent.putExtra(MESSAGE, utterance)
                    startService(outgoingIntent)
                }else{
                    stopSelf()
                }
            }
        }catch(exception: Exception){
            Log.e("There was an error trying to process the text")
        }
    }

    fun checkRegex(){

    }

    // This should be called *after* the classification? Although, the entities could cut across skills....
    // This could also be done independantly.....
    fun findEntities(utterance: String, category: String){

    }

    fun loadClassifier(): ColumnDataClassifier?{
        var classifierFile = File(filesDir,"intent.classifier")
        if(classifierFile.exists() != true) {
            var trainingIntent = Intent()
            // This should work...
            trainingIntent.setClassName(PACKAGE_NAME,"${PACKAGE_NAME}.processor.ProcessorTrainingService")
            startService(trainingIntent)
            return null
        }else{
            return ColumnDataClassifier.getClassifier(classifierFile.canonicalPath)
        }
    }

    fun deleteClassifier(){
        var file = File(filesDir,"intent.classifier")
        file.delete()
    }
}