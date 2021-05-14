package net.carrolltech.athena.processor

import android.content.Intent
import android.os.IBinder
import edu.stanford.nlp.classify.ColumnDataClassifier
import net.carrolltech.athena.framework.SapphireFrameworkService
import java.io.*
import java.lang.Exception
import java.util.*

class ProcessorTrainingService: SapphireFrameworkService(){
    var INITIALIZE = "action.athena.skill.INITIALIZE"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            Log.v("ProcessorTrainingService started")
            train(intent)
        }catch(exception: Exception){
            exception.printStackTrace()
            Log.d("There was an error with the received intent. It was lacking some stuff, I suspect")
        }

        return super.onStartCommand(intent, flags, startId)
    }


    // Hmmm... Too hardcoded?
    var DIALOG = "dialog"
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
        /*
        Get the list of assistant packages
        // Will be RAW files, labeled in the XML
        load the @[<package_name>:]<resource_type>/<resource_name>
        combineFiles()
         */

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

    fun train(intent: Intent?) {
        try {
            var combinedFile = getTrainingFile(INTENT)
            // Currently unused. It's for Mycroft style .intent files to meet Stanford CoreNLP standards
            //var prepared = prepareTrainingData(intent!!)
            var properties = createProperties()
            var classifier = ColumnDataClassifier(properties)

            Log.i("Commencing training...")
            classifier.trainClassifier(combinedFile.canonicalPath)
            Log.i("Intent classifier training done")
            saveClassifier(classifier)

            // Return to the main Processor portion
            var intent = Intent().setClassName(this,"net.carrolltech.athena.processor.ProcessorService")
            startService(intent)
        } catch (exception: Exception){
            exception.printStackTrace()
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

    // This is where saveClassifier is called
    fun trainIntentClassifier(trainingFiles: List<String>){
        var properties = createProperties()
        var classifier = ColumnDataClassifier(properties)
        var combinedFile = combineFiles(trainingFiles)

        classifier.trainClassifier(combinedFile.canonicalPath)
        Log.i("Intent classifier training done")
        saveClassifier(classifier)
    }

    fun saveClassifier(classifier: ColumnDataClassifier){
        val fileName = File(this.filesDir,"intent.classifier")
        classifier.serializeClassifier(fileName.canonicalPath)
    }

    // This shouldn't be hardcoded, and should be moved to a file
    fun createProperties(): Properties{
        var properties = Properties()
        properties.setProperty("goldAnswerColumn","0")
        properties.setProperty("useNB","true")
        //props.setProperty("useClass","true")
        properties.setProperty("useClassFeature","true")
        //props.setProperty("1.splitWordsRegexp","false")
        //props.setProperty("1.splitWordsTokenizerRegexp","false")
        properties.setProperty("1.splitWordsWithPTBTokenizer","true")
        // This is the line that was missing
        properties.setProperty("1.useSplitWords","true")
        return properties
    }
}