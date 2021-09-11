package net.carrolltech.athena.processor

import android.animation.TimeAnimator
import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate
import edu.stanford.nlp.ie.NERFeatureFactory
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.AnnotationPipeline
import edu.stanford.nlp.pipeline.POSTaggerAnnotator
import edu.stanford.nlp.pipeline.TokenizerAnnotator
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator
import edu.stanford.nlp.sequences.ColumnDocumentReaderAndWriter
import edu.stanford.nlp.time.TimeAnnotator
import edu.stanford.nlp.util.CoreMap
import kotlinx.coroutines.newFixedThreadPoolContext
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

class TemporaryEntityExtractor: SapphireFrameworkService() {
    var INITIALIZE = "action.athena.skill.INITIALIZE"
    var INTENT = "intent"
    var ENTITY = "entity"

    lateinit var intentFiles: List<String>
    lateinit var entityFiles: List<String>

    lateinit var classifier: CRFClassifier<CoreLabel> // = loadClassifier()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("Intent received")
        when(intent?.action){
            "action.athena.TEST" -> testCode(intent)
            "action.athena.TEST_INPUT" -> testInput(intent)
            else -> Log.v("Guess this intent does nothing")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Currently, the test code should get all assets, and train the classifier on it
    fun testCode(intent: Intent){
        // Request all intent and entity files
        intentFiles = getAssetFiles(INTENT)
        entityFiles = getAssetFiles(ENTITY)
        Log.v("Assets gathered")
        Log.v("${intentFiles}\n${entityFiles}")

        if(intentFiles.isNotEmpty() and entityFiles.isNotEmpty()) {
            // This returns a File
            var intentFile = combineFiles(intentFiles)
            var expandedIntents = convertIntentToEntityTrainingFile(intentFile, entityFiles)
            trainClassifier(expandedIntents)
        }
    }

    fun testInput(intent: Intent){
        var intentText = intent.getStringExtra("text")!!
        Log.v("Intent had text: ${intentText}")
        var tempFile = File(cacheDir, "tempFile")
        tempFile.writeText(intentText)

        //var tokens = intentText.split(" ")
        var catcher = classifier.classifyAndWriteAnswers(tempFile.canonicalPath,classifier.plainTextReaderAndWriter(),true)
        Log.v("Result: ${catcher.asList()}")
    }

    fun convertIntentToEntityTrainingFile(intentsFile: File, entityFiles: List<String>): File{
        // This is the intents file
        var formatted = mutableListOf<Pair<String,String>>()
        // This is only set up for a single entity per sentence. Likely will need adjustment

        //For every sentence, expand the sentence
        intentsFile.forEachLine { line ->
            // This is so it's a clean list each time
            var cleanedTokenList = mutableListOf<String>()
            //  This may not be the best place to declare this
            var entityPair: Pair<String,Int>? = null
            // Split it in to tokens
            var tokens = line.split(" ")
            var index = 0
            for(token in tokens){
                var cleanedToken = token
                // Get rid of the class. This is ugly, but should work
                if(token.contains("\t")){
                    cleanedToken = token.substringAfter("\t")
                }
                Log.v(cleanedToken)
                cleanedTokenList.add(cleanedToken)
                if(cleanedToken.startsWith("{")) {
                    // Save the index
                    index = tokens.indexOf(token)
                    entityPair = Pair<String,Int>(token,index)
                    Log.v("Found a token that starts with a {. It's ${entityPair.first} at index ${entityPair.second}")
                }
            }

            // If one of them needed to be expanded, then expand it
            if(entityPair != null){
                Log.v("Expaning intent for entities")
                // this will be for the entity file, but I haven't changed it yet
                var newRecords = mutableListOf<Pair<String, String>>()
                for(token in cleanedTokenList){
                    newRecords.add(Pair<String,String>(token,"O"))
                }
                Log.v("This is what the newRecords looks like: ${newRecords.toString()}")
                var entityFilename = loadEntityFile(entityPair.first)
                Log.v("Entity filename: ${entityFilename.toString()}")
                if (entityFilename != null) {
                    // load the entity file
                    var entityFile = File(cacheDir,entityFilename)
                    // for every entity word in the file
                    entityFile.forEachLine { entity ->
                        //new records should be a copy of the prior sentence. Basically, the cleanedToken list
                        newRecords.set(index, Pair(entity, entityFilename.substringBefore(".")))
                        formatted.addAll(newRecords)
                        // This adds a space to break up documents
                        formatted.add(Pair("%%","%%"))
                        Log.v("Added the %%")
                    }
                }else{
                    Log.e("There doesn't seem to be an entity file for that entity")
                }
            }else{
                for(token in cleanedTokenList){
                    formatted.add(Pair(token,"O"))
                }
                // If there is not an entity in the sentence, add this to the file
                formatted.add(Pair("%%","%%"))
                Log.v("Added the %%")
            }
        }

        Log.v("Intents expanded")
        var outFile = File(cacheDir,"outfile")
        formatted.forEach { row ->
            // This should write it in proper format. I need a space to break up documents
            if((row.first == "%%")and(row.second == "%%")){
                Log.v("Found %%. There should be a new line here")
                outFile.appendText("\n")
            }else{
                Log.v("${row.first}\t${row.second}\n")
                outFile.appendText("${row.first}\t${row.second}\n")
            }
        }
        return outFile
    }

    fun loadEntityFile(entityName: String): String?{
        var formatted = entityName.substring(1,entityName.length-1)
        formatted = "${formatted}.entity.temp"
        Log.v("Formatted entity filename: ${formatted}")
        for(index in entityFiles){
            Log.v("Checking ${index} for match with ${formatted}")
            if(formatted == index){
                return index
            }
        }
        return null
    }

    // These are defaults copied from https://nlp.stanford.edu/software/crf-faq.html
    fun useDefaultProps(): Properties{
        var props = Properties()
        props.setProperty("UseClassFeature","true")
        props.setProperty("useWord","true")
        props.setProperty("useNGrams","true")
        props.setProperty("noMidNGrams","true")
        props.setProperty("maxNGramLeng","6")
        props.setProperty("usePrev","true")
        props.setProperty("useNext","true")
        props.setProperty("useSequences","true")
        props.setProperty("usePrevSequences","true")
        props.setProperty("maxLeft","1")
        props.setProperty("useTypeSeqs","true")
        props.setProperty("useTypeSeqs2","true")
        props.setProperty("useTypeySequences","true")
        props.setProperty("wordShape","chris2useLC")
        props.setProperty("useDisjunctive","true")
        return props
    }

    fun useCustomProps(): Properties {
        var props = Properties()
        props.setProperty("useWord","true")
        props.setProperty("useSequences","false")
        //props.setProperty("useSum","true")
        props.setProperty("useNB","true")
        props.setProperty("usePrev","true")
        props.setProperty("useNext","true")
        return props
    }

    fun trainClassifier(expandedIntents: File){
        // This is a terrible variable name, but it's temporary
        var filename = expandedIntents.canonicalPath
        var props = useDefaultProps()
        //var props = useCustomProps()

        var reader = ColumnDocumentReaderAndWriter()
        // This just needs to be edited to read a two column document, rather than three column
        reader.init("word=0,answer=1")

        classifier = CRFClassifier<CoreLabel>(props)
        classifier.train(filename, reader)
        Log.v("Classifier trained")
    }

    fun saveClassifier(classifier: CRFClassifier<CoreLabel>){
        classifier.serializeClassifier("path")

    }

    fun loadClassifier(): CRFClassifier<CoreLabel>{
        return CRFClassifier.getClassifier("filename")
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

    // This is called from inside getAssetFile
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

    // This is rough. I may want to touch this up
    fun combineFiles(files: List<String>): File{
        var combinedFile = File.createTempFile("trainingFile",".tmp", cacheDir)

        for (filename in files) {
            var file = File(cacheDir, filename)
            for (line in file.readLines()) {
                Log.i("Line being added: ${line.trim()}")
                // I need to be careful. I could be adding unneeded white space
                combinedFile.appendText("${line.trim()}\n")
            }
        }
        Log.v("File combined")
        return combinedFile
    }
}