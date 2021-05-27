package net.carrolltech.athena.processor

import android.content.Intent
import edu.stanford.nlp.ie.crf.CRFClassifier
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
        var properties = getProperties()

        // I need to expand known sents, and expand training data
        var expanded = expandSentences()

        var trainingFilepath = getTrainingFilepathFromStrings(expanded)
        // The properties are those of a NERFeatureFactory, since the CRFClassifer uses one by default
        // Is there a reason it's not defined as a <CoreMap> by default?
        var crfClassifier = CRFClassifier<CoreMap>(properties)
        // hmmm....
        crfClassifier.train(trainingFilepath)
        var classifierFilepath = File(cacheDir,"entityClassifier.crf").absolutePath
        crfClassifier.serializeClassifier(classifierFilepath)
    }

    fun getTrainingFilepathFromStrings(sentences: Collection<String>): String{
                var cacheFile = File(cacheDir,"cacheEntityFile")

        for(sentence in sentences){
            cacheFile.writeText("${sentence}\n")
        }
        var filepath = cacheFile.absolutePath

        return filepath
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
        var properties = getProperties()


        // The properties are those of a NERFeatureFactory, since the CRFClassifer uses one by default
        // Is there a reason it's not defined as a <CoreMap> by default?
        var crfClassifier = CRFClassifier<CoreMap>(properties)
        // hmmm....
        var filepath = File(cacheDir,"filename").absolutePath
        crfClassifier.train(filepath)
        crfClassifier.serializeClassifier(filepath)
    }

    // hmmm....
    /*
     I am removing the word as a feature to help reduce overfitting, I am concerned about
     repetitive sentences though, since they'll cause the classifier to overfit anyway. That said,
     should I add noise?
     */
    fun checkForWildcardEntity(){
        var properties = getProperties()

        // The properties are those of a NERFeatureFactory, since the CRFClassifer uses one by default
        // Is there a reason it's not defined as a <CoreMap> by default?
        var crfClassifier = CRFClassifier<CoreMap>(properties)
        // hmmm....
        var filepath = File(cacheDir,"filename").absolutePath
        crfClassifier.train(filepath)
        crfClassifier.serializeClassifier(filepath)
    }

    fun getWildcardProperties(): Properties{
        var properties = getProperties()
        properties.setProperty("useWord","false")
        return properties
    }

    fun getProperties(): Properties{
        var properties = Properties()

        /*
         This tells the CRFClassifier what is in each column
         This can be used to add features (such as other tags) if pipelining
         Might also be able to use it to set a generic/wildcard feature
         */
        properties.setProperty("map","word=0,answer=1")
        properties.setProperty("usePrev","true")
        properties.setProperty("useNext","true")

        return properties
    }

    var INITIALIZE = "action.athena.skill.INITIALIZE"

    fun getAssetFiles(type: String): List<String>{

        // This will exist in *every* Athena skill
        var intent = Intent().setAction(INITIALIZE)
        var filenames = mutableListOf<String>()
        // Get all of Athena's skills
        Log.v("Querying packages")
        var queryResults = this.packageManager.queryIntentServices(intent,0)
        Log.v("${queryResults.size} results found")
        for(resolveInfo in queryResults){
            var packageName = resolveInfo.serviceInfo.packageName
            var packageResources = this.packageManager.getResourcesForApplication(packageName)
            var assetsStuff = packageResources.assets
            for(filename in assetsStuff.list("")!!){
                Log.v("File to check: ${filename}")
                if(filename.endsWith(type)){
                    var inputStream = assetsStuff.open(filename)
                    filenames.add(convertAssetToFile(inputStream, filename))
                    Log.v("Converted ${filename} to file")
                }
            }
        }
        return filenames
    }

    var INTENT = "intent"
    var ENTITY = "entity"

    fun getTrainingFile(type: String): File{
        var files = getAssetFiles(INTENT)
        var file = combineFiles(files)
        return file
    }

    fun convertAssetToFile(inputStream: InputStream, filename: String): String{
        Log.v("Converting resource to file")
        try{
            var data = inputStream.read()
            var cacheFile = File(cacheDir,"${filename}.temp")
            var cacheFileWriter = cacheFile.outputStream()

            while(data != -1){
                cacheFileWriter.write(data)
                data = inputStream.read()
            }
            cacheFileWriter.close()
            Log.v("File converted")
            return cacheFile.name
        }catch (exception: Exception){
            exception.printStackTrace()
            return ""
        }
    }

    fun expandSentences(): List<String>{
        var files = requestFiles()

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

        return inflated
    }

    fun inflateEntitySentences(): MutableList<String>{
        return mutableListOf()
    }
}