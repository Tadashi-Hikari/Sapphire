package net.carrolltech.athena.processor

import android.content.Intent
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.util.CoreMap
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File
import java.io.InputStream
import java.util.*

class ProcessorEntityTrainingService : SapphireFrameworkService() {

    var INITIALIZE = "action.athena.skill.INITIALIZE"
    var INTENT = "intent"
    var ENTITY = "entity"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "action.athena.TEST" -> testCode(intent)
            else -> null
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is the filename and lines of each entity file
    var entityMap = mutableMapOf<String,List<String>>()
    // This is the filename and lines of each intent file
    var intentMap = mutableMapOf<String,List<String>>()

    fun testCode(intent: Intent){
        var intentFiles = getAssetFiles(INTENT)
        var entityFiles = getAssetFiles(ENTITY)

        var expandedPair = recursiveExpandSentences(emptyList())
        // the first one can be used for training the intent parser (without wildcards), or a regex one.
        // The second is the one formatted for training the entity extractor
        var trainingFilepath = convertStringsToFile(expandedPair.second)

        var properties = getProperties()
        // The properties are those of a NERFeatureFactory, since the CRFClassifer uses one by default
        // Is there a reason it's not defined as a <CoreMap> by default?
        var crfClassifier = CRFClassifier<CoreMap>(properties)
        // hmmm....
        crfClassifier.train(trainingFilepath)

        var classifierFilepath = File(cacheDir,"entityExtractor").absolutePath
        crfClassifier.serializeClassifier(classifierFilepath)

    }

    fun convertStringsToFile(stringList: List<String>): String{
        var file = File(cacheDir, "stringCache")

        for(line in stringList){
            file.writeText("${line}\n")
        }

        return file.absolutePath
    }

    // Might as well run this through the test, and see how it works
    fun recursiveExpandSentences(sentences: List<String>): Pair<List<String>,List<String>>{
        // These are the sentences to train on
        var expandedSentences = mutableListOf<String>()
        // This is formatted for training the CRFClassifier
        var formattedSentences = mutableListOf<String>()

        for(entity in entityMap){
            for(sentence in sentences){
                var tokenizer = StringTokenizer(sentence)
                //
                var index = 0
                while(tokenizer.hasMoreTokens()){
                    var token = tokenizer.nextToken()
                    // This should directly match the internal entity?
                    if(token.regionMatches(1,entity.key,1,entity.key.length)){
                        for(value in entity.value) {
                            var selfTokenized = sentence.split("\s") as MutableList
                            selfTokenized.set(index,value)
                            // This should do what I need it to. Should I just use this instead of the Java tokenizer?
                            expandedSentences.add(selfTokenized.joinToString(" "))
                            Log.v(selfTokenized.joinToString(" "))
                        }
                        var returnedPair = recursiveExpandSentences(expandedSentences)
                        // Is this needlessly bulky?
                        // This is going to return [] stuff, that should be replaced/flushed out...
                        expandedSentences.addAll(returnedPair.first)
                        formattedSentences.addAll(returnedPair.second)
                    }
                    index++
                }
            }
        }
        return Pair(expandedSentences,formattedSentences)
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
        //regexEntity()
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

    fun getTrainingFile(type: String): File{
        var entityFiles = getAssetFiles(ENTITY)
        var intentFiles = getAssetFiles(INTENT)
        // How exactly am I expecting to expand this?
        var something = expandSentences(entityFiles)

        return file
    }

    // Isn't this duplicated elsewhere...?
    fun requestFiles(){
        getAssetFiles("filename")
    }

    fun expandSentences(): List<String>{
        // This is always requesting assets right?
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

    fun inflateEntitySentences(){
        // I don't remember the purpose of this
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
}