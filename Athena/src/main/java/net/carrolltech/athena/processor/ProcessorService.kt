package net.carrolltech.athena.processor

import android.content.Intent
import android.view.View
import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
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
            if(utterance == "delete"){
                deleteClassifier()
            }else if(utterance != ""){
                Log.i("Loading the classifier")
                var intentClassifier = loadIntentClassifier()
                var entityClassifier = loadEntityClassifier()
                if(intentClassifier != null) {
                    // I can probably move away from files, but I don't want to mess with that until it is working.
                    var utteranceFile = File(cacheDir,"utteranceFile")
                    utteranceFile.writeText(utterance!!)
                    var catcher = entityClassifier.classifyAndWriteAnswers(utteranceFile.canonicalPath,entityClassifier.plainTextReaderAndWriter(),true)
                    // This is specific to how CoreNLP works
                    var datumToClassify = intentClassifier.makeDatumFromLine("none\t${utterance}")
                    // Can these two be combined, or done at the same time?
                    var classifiedDatum = intentClassifier.classOf(datumToClassify)
                    var classifiedScores = intentClassifier.scoresOf(datumToClassify)
                    Log.v("Datum classification: ${classifiedDatum}")
                    Log.v("Entities: ${catcher}")
                    // This is an arbitrary number, and should probably be a configurable variable
                    if (classifiedScores.getCount(classifiedDatum) >= .6) {
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

    fun loadIntentClassifier(): ColumnDataClassifier?{
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

    fun trainEntityClassifier(){
        var testIntent = Intent().setAction("action.athena.TEST")
        testIntent.setClassName(this,"net.carrolltech.athena.processor.ProcessorEntityTrainingService")
        startService(testIntent)
    }

    fun loadEntityClassifier(): CRFClassifier<CoreLabel>{
        var entityClassifierFile = File(filesDir,"entity.classifier")
        return CRFClassifier.getClassifier(entityClassifierFile.canonicalPath)
    }

    fun deleteClassifier(){
        var file = File(filesDir,"intent.classifier")
        file.delete()
    }
}