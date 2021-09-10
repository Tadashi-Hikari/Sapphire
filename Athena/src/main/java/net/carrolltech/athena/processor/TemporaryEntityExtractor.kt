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

    var intentFiles = getAssetFiles(INTENT)
    var entityFiles = getAssetFiles(ENTITY)

    var classifier = loadClassifier()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("Intent received")
        when(intent?.action){
            "action.athena.TEST" -> testCode(intent)
            "action.athena.TEST_INPUT" -> testInput(intent)
            else -> null
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun testCode(intent: Intent){
        getTrainingFile(INTENT)
        getTrainingFile(Entity)

        trainClassifier()
    }

    fun testInput(intent: Intent){
        var catcher = classifier.classify(intent.getStringExtra("text"))
        Log.v("Result ${catcher}")
    }

    // This should definitely be recursive
    fun convertIntentToEntityTrainingFile(){
        var file = File("filename")
        var formatted = listOf<Pair<String,String>>()
        file.forEachLine { line ->
            // Split it in to tokens
            var tokens = line.split(" ")
            var index = 0
            for(token in tokens){
                Log.v(token)
                if(token.startsWith("{")) {
                    // Save the index
                    index = tokens.indexOf(token)
                }
            }
            // If one of them needed to be expanded, then expand it
            if(index != 0){
                // this will be for the entity file, but I haven't changed it yet
                var newRecords = mutableListOf<Pair<String,String>>()
                file.forEachLine { entity ->
                    newRecords.addAll(formatted)
                    newRecords.set(index,Pair(entity,"entityType"))
                }
                formatted = newRecords
            }
        }
        var outFile = File("outfile")
        formatted.forEach { row ->
            // This should write it in proper format.
            outFile.writeText("${row.first}\t${row.second}\n")
        }
    }

    fun trainClassifier(){
        convertIntentToEntityTrainingFile()

        var reader = ColumnDocumentReaderAndWriter()
        // This just needs to be edited to read a two column document, rather than three column
        reader.init("word=0,answer=1")

        // Well, lets see if this works...
        var crfClassifier = CRFClassifier.getClassifier(CRFClassifier.DEFAULT_CLASSIFIER)
        crfClassifier.train("filename", reader)
    }

    fun saveClassifier(classifier: CRFClassifier<CoreLabel>){
        classifier.serializeClassifier("path")

    }

    fun loadClassifier(): CRFClassifier<CoreLabel>{
        return CRFClassifier.getClassifier(CRFClassifier.DEFAULT_CLASSIFIER)
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

    // This is rough. I may want to touch this up
    fun combineFiles(files: List<String>): File{
        var combinedFile = File.createTempFile("trainingFile",".tmp", cacheDir)

        for(filename in files){
            var file = File(cacheDir,filename)
            for(line in file.readLines()){
                Log.i("Line being added: ${line.trim()}")
                // I need to be careful. I could be adding unneeded white space
                combinedFile.appendText("${line.trim()}\n")
            }
        }
        Log.v("File combined")
        //combinedFile.
        return combinedFile
    }
}