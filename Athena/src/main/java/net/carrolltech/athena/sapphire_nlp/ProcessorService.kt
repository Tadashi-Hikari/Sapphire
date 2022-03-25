package net.carrolltech.athena.sapphire_nlp

import android.content.Intent
import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import net.carrolltech.athena.sapphire_framework.SapphireFrameworkService
import net.carrolltech.athena.sapphire_framework.SapphireUtils
import java.io.File

/***
 * This module exists to coordinate the usage and training of the intent and entity classifiers.
 * It is possible they may be used independently, need retraining, or potentially could become
 * a service for other modules (including loading custom classifiers), so this landing helps
 * to coordinate that
 */

class ProcessorService: SapphireFrameworkService(){
    var id = "1"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            Log.v("ProcessorCentralService started")
            when(intent?.action){
                SapphireUtils().ACTION_SAPPHIRE_PROCESSOR_TRAIN -> trainClassifier()
                else -> process(intent)
            }
        }catch (exception: Exception){
            Log.d("There was an intent error w/ the processor")
            exception.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // This should be renamed, definitely
    // This is handling running both things concurrently. It could probably be more elegant
    // it looks hacked together
    fun process(intent: Intent?){
        var utterance = ""
        if(intent?.hasExtra(SapphireUtils().MESSAGE) == true) {
            utterance = intent.getStringExtra(SapphireUtils().MESSAGE)!!
        }
        var outgoingIntent = Intent()

        try{
            // This should be moved off to a command file?
            if(utterance == "delete"){
                deleteClassifier()
            }else if(utterance != ""){
                Log.i("Loading the classifier")
                var intentClassifier = loadIntentClassifier()
                //var entityClassifier = loadEntityClassifier()
                if(intentClassifier != null) {
                    // I can probably move away from files, but I don't want to mess with that until it is working.
                    var utteranceFile = File(cacheDir,"utteranceFile")
                    utteranceFile.writeText(utterance!!)
                    //var catcher = entityClassifier.classifyAndWriteAnswers(utteranceFile.canonicalPath,entityClassifier.plainTextReaderAndWriter(),true)
                    //var entityList = cleanEntityList(catcher.toString())
                    //Log.v("Cleaned entity list: ${entityList.toString()}")
                    // This is specific to how CoreNLP works
                    var datumToClassify = intentClassifier.makeDatumFromLine("none\t${utterance}")
                    // Can these two be combined, or done at the same time?
                    var classifiedDatum = intentClassifier.classOf(datumToClassify)
                    var classifiedScores = intentClassifier.scoresOf(datumToClassify)
                    Log.v("Datum classification: ${classifiedDatum}")
                    //Log.v("Entities: ${catcher}")
                    // This is an arbitrary number, and should probably be a configurable variable
                    if (classifiedScores.getCount(classifiedDatum) >= .6) {
                        Log.i("Text matches class ${classifiedDatum}")
                        TimeDateConverter().testing("something")
                        outgoingIntent.putExtra(SapphireUtils().ROUTE, classifiedDatum)
                    } else {
                        Log.i("Text does not match a class. Using default")
                        outgoingIntent.putExtra(SapphireUtils().ROUTE, "DEFAULT")
                    }
                    //outgoingIntent.putStringArrayListExtra("ENTITIES",entityList)
                    outgoingIntent.putExtra(SapphireUtils().MESSAGE, utterance)
                    outgoingIntent.setClassName(this,SapphireUtils().CORE_SERVICE)
                    startService(outgoingIntent)
                }else{
                    outgoingIntent.putExtra(SapphireUtils().ID,id)
                    startService(outgoingIntent)
                }
            }
        }catch(exception: Exception){
            Log.e("There was an error trying to process the text")
            Log.e(exception.stackTraceToString())
        }
    }

    fun loadIntentClassifier(): ColumnDataClassifier?{
        var classifierFile = File(filesDir,"intent.classifier")
        if(classifierFile.exists() != true) {
            var trainingIntent = Intent()
            // This should work...
            trainingIntent.setClassName(PACKAGE_NAME,SapphireUtils().CLASSIFIER_TRAINING_SERVICE)
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

    fun trainClassifier(){
        var intent = Intent().setClassName(this,SapphireUtils().CLASSIFIER_TRAINING_SERVICE)
        startService(intent)
    }

    fun trainEntityClassifier(){
        var testIntent = Intent().setAction("action.athena.TEST")
        testIntent.setClassName(this,SapphireUtils().ENTITY_TRAINING_SERVICE)
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