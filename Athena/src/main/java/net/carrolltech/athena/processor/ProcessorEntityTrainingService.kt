package net.carrolltech.athena.processor

import android.content.Intent
import edu.stanford.nlp.ie.NERFeatureFactory
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.util.CoreMap
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File
import java.util.*

class ProcessorEntityTrainingService : SapphireFrameworkService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "action.athena.TEST" -> testCode(intent)
            else -> null
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun testCode(intent: Intent){
        entityPipeline()
    }

    fun trainEntityPipeline(){

    }

    fun entityPipeline(): List<String>{
        // I need the confidence, and I need the token/location?
        var tokens = emptyList<String>()
        var enitity = mutableListOf<String>()

        // Each one of these returns a list
        regexEntity()
        checkForKnownEntity()
        checkForWildcardEntity()

        // This does the combination
        for(index in tokens){
            if(index != "0") {
                var max = getMax(1, 2, 3)
                when (max) {
                    1 -> enitity.add("1")
                    2 -> enitity.add("2")
                    3 -> enitity.add("3")
                }
            }
        }
        return enitity
    }

    // I don't know how I feel about this
    fun getMax(one: Int, two: Int, three: Int): Int{
        var temp = Math.max(one,two)
        return Math.max(temp,three)
    }

    fun regexEntity(){

    }

    fun checkForKnownEntity(){

    }

    fun checkForWildcardEntity(){
        var properties = Properties()

        // I believe this gives the tagging features for the classifier itself?
        var NERfeatures = NERFeatureFactory<CoreLabel>()
        var stuff = NERfeatures

        // Is there some reason that this doesn't define CoreMap by default? Check for bugs here
        var crfClassifier = CRFClassifier<CoreMap>(properties)
    }

    fun trainWildcardEntities(){

    }

    fun convertEntities(){
        // if {entity} matches Filename
        //   -train using filename generated sentences w/ NERFeatureFactory
        // else
        //   - train using positional/contextual info

        // This will be tokenized?
        var sentence = "This is a sentence"
        var inflated = mutableListOf<String>()
        var file = File(cacheDir,"testfile")
        for(word in sentence) {
            if (word.toString().startsWith("{")) {
                //extract word
                var extracted = "entity".toUpperCase() // I don't know that I need to do this
                inflated.addAll(inflateEntitySentences())
            }
        }

        // This will only work for know entities
        for(sentence in inflated){
            for(word in sentence) {
                file.writeText("${word}\t0")
            }
        }
    }

    fun inflateEntitySentences(): MutableList<String>{
        return mutableListOf()
    }

    fun entityExtraction(){
        // for each .entity, expand
        // for {wildcard}, positional?
        //  -useGenericFeatures
    }
}