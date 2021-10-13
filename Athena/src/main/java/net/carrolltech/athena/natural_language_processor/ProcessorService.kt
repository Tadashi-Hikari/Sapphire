package net.carrolltech.athena.natural_language_processor

import android.content.Intent
import android.os.IBinder
import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File

/***
 * This module exists to coordinate the usage and training of the intent and entity classifiers.
 * It is possible they may be used independently, need retraining, or potentially could become
 * a service for other modules (including loading custom classifiers), so this landing helps
 * to coordinate that
 */

class ProcessorService: SapphireFrameworkService(){
    // I don't see any reason why this needs a PendingIntent over just a bound service
    override fun onBind(intent: Intent?): IBinder? {
        try{
            Log.v("ProcessorCentralService started")
            when {
                else -> process(intent)
            }
        }catch (exception: Exception){
            Log.d("There was an intent error w/ the processor")
           exception.printStackTrace()
        }

        // Is this an issue, that the stuff here could be long running?
        return null
    }

    // This should be renamed, definitely
    // This is handling running both things concurrently. It could probably be more elegant
    // it looks hacked together
    fun process(intent: Intent?){
        var utterance = intent!!.getStringExtra(MESSAGE)
        var outgoingIntent = Intent()

        try{
            // This should be moved off to a command file?
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
                    var entityList = cleanEntityList(catcher.toString())
                    Log.v("Cleaned entity list: ${entityList.toString()}")
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
                        TimeDateConverter().testing("something")
                        outgoingIntent.putExtra(ROUTE, classifiedDatum)
                    } else {
                        Log.i("Text does not match a class. Using default")
                        outgoingIntent.putExtra(ROUTE, "DEFAULT")
                    }
                    outgoingIntent.putStringArrayListExtra("ENTITIES",entityList)
                    outgoingIntent.putExtra(MESSAGE, utterance)
                    outgoingIntent.setClassName(this,"net.carrolltech.athenaalarmskill.simpleAlarmService")
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

    fun cleanEntityList(entityCatcher: String): ArrayList<String>{
        var entityList = mutableListOf<String>()
        var tokens = entityCatcher.split(" ")
        for(token in tokens){
            var splitToken = token.split("\\")
            if(splitToken[1] != "O"){
                entityList.add(splitToken[0])
            }
        }
        return entityList as ArrayList<String>
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