package net.carrolltech.athena.processor

import android.content.Intent
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.util.CoreMap
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File
import java.io.InputStream
import java.util.*

class temp: SapphireFrameworkService(){


    fun runEntityExtractor(utterance: String){
        var properties = getProperties()

        var filepath = File(cacheDir,"entityExtractor").absolutePath
        var crfClassifier = CRFClassifier<CoreMap>(properties)
        crfClassifier.loadClassifier(filepath)
        var entities = crfClassifier.classify(utterance)
        Log.d(entities.toString())
    }

    fun trainEntityExtractor(){
        // These are holding file paths, not the filedata themselves
        // If I am going to return the internal information, how do I track the filename/entity type?
        var entities = getAssetFiles("entity")
        var sentences = getAssetFiles("intent")

        // This may overload/overfit the classifier w/ repetitive sentences
        // This is taking lists of strings, not expecting them to be file strings
        var expandedSentences = expandSentences(sentences, entities)
        // Could/should this be collapsed in to?
        var trainingFilepath = convertStringsToFile(expandedSentences)

        var properties = getProperties()


        // The properties are those of a NERFeatureFactory, since the CRFClassifer uses one by default
        // Is there a reason it's not defined as a <CoreMap> by default?
        var crfClassifier = CRFClassifier<CoreMap>(properties)
        // hmmm....
        crfClassifier.train(trainingFilepath)

        var classifierFilepath = File(cacheDir,"entityExtractor").absolutePath
        crfClassifier.serializeClassifier(classifierFilepath)
    }

    fun convertStringsToFile(sentences: List<String>): String{
        Log.v("Converting resource to file")
        var filename = "temporary"

        try{
            var cacheFile = File(cacheDir,"${filename}.temp")

            // Just add them all in, why don't you?
            for(sentence in sentences) {
                cacheFile.writeText("${sentence}\n")
            }

            Log.v("File converted")
            return cacheFile.name
        }catch (exception: Exception){
            exception.printStackTrace()
            return ""
        }
    }

    /*
        I need... to take all sentences w/ {entities} and match them to the entity using the CRF
        This means I need to covert the sentences to usable/prepared entity files. Expanding them
        by adding in the words, and then formatting them
    */
    fun expandSentences(intents: List<String>, entities: List<String>): List<String>{
        var formattedSentences = mutableListOf<String>()

        // This is the filename/type, and the lines
        var entityMap = mutableMapOf<String,List<String>>()
        // I don't think that I need to track which intent I am following, just the entity
        var intentLines = mutableListOf<String>()

        // I could see this running in O^n time, so I need to be careful
        // I need to extract the sentences. I can probably optimize this
        for(entityFilepath in intents){
            var entityFile = File(cacheDir,entityFilepath)
            var entityTokens = mutableListOf<String>()
            // This should take
            for(line in entityFile.readLines()){
                // I need to be sure this isn't stripping the whitespace
                entityTokens.add(line)
            }
            // This should create
            entityMap.put(entityFile.name,entityTokens)
        }

        for(intentFilepath in intents) {
            var intentFile = File(cacheDir, intentFilepath)
            // This should take
            for (line in intentFile.readLines()) {
                // I need to be sure this isn't stripping the whitespace
                intentLines.add(line)
            }
        }

        // I need to replace all entitiesTokens w/ the entity themselves
        // This should actually be a recursive function, that calls itself whenever an entity is found in a sentence
        for(currentEntityMap in entityMap){
            for(line in intentLines){

                //pollute(updatedSentence)?
                var stringTokenizer = StringTokenizer(line)

                var newSentence = ""

                // This is handling the formatting of the sentences themselves.
                // I need to expand the sentences before formatting
                for(word in stringTokenizer){
                    when(word){
                        // mark this as the entity
                        entity -> newSentence+"${word}\t${entity.toUpperCase()}"
                        else -> newSentence+"${word}\tO"
                    }
                    if(stringTokenizer.hasMoreTokens()){
                        // This separtes tokens/words within a sentence
                        newSentence+="\n"
                    }
                    // This is a fully formatted sentence, to be added in to the setup
                }
                // Once the words have been passed through, add the sentence
                // New documents Must be separated by a single blank line. This is how the classifier separates them
                // This should create the additional space, though I may want to print this for reference
                formattedSentences.add(newSentence+"\n")
            }
            //pollute(updatedSentence)?
        }

        return formattedSentences
    }

    var INITIALIZE = "action.athena.skill.INITIALIZE"
    var INTENT = "intent"
    var ENTITY = "entity"

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

    fun getProperties(): Properties {
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

    fun getWildcardProperties(): Properties{
        var properties = getProperties()
        properties.setProperty("useWord","false")
        return properties
    }
}