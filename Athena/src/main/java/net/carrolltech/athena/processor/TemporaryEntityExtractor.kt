package net.carrolltech.athena.processor

import android.animation.TimeAnimator
import android.util.Log
import edu.stanford.nlp.ie.NERFeatureFactory
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.pipeline.AnnotationPipeline
import edu.stanford.nlp.pipeline.POSTaggerAnnotator
import edu.stanford.nlp.pipeline.TokenizerAnnotator
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator
import edu.stanford.nlp.sequences.ColumnDocumentReaderAndWriter
import edu.stanford.nlp.time.TimeAnnotator
import edu.stanford.nlp.util.CoreMap
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.File
import java.lang.StringBuilder
import java.util.*

class TemporaryEntityExtractor {
    fun kitchenSink(){
        // input is one token per line, columns indicate word, pos, chunk, and answer class (maybe not all?)
        var col = ColumnDocumentReaderAndWriter()
        // I don't think I need to do anything with this, unless I want to change the defaults
        //var features = NERFeatureFactory
        // Remember, properties aren't features
        var properties = Properties()
        var props = Properties()
        // This is the time annotator
        var pipeline = AnnotationPipeline()
        pipeline.addAnnotator(TimeAnnotator("sutime",props))
        pipeline.addAnnotator(TokenizerAnnotator(false))
        pipeline.addAnnotator(WordsToSentencesAnnotator(false))
        pipeline.addAnnotator(POSTaggerAnnotator(false))
    }

    // This should definitely be recursive
    fun convertEntity(){
        var file = File("filename")
        var formatted = listOf<Pair<String,String>>()
        file.forEachLine { line ->
            // Split it in to tokens
            var tokens = line.split(" ")
            var index = 0
            for(token in tokens){
                Log.v("Here's a token:",token)
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

    fun loadEntityList(): List<String>{
        return emptyList()
    }

    fun convertSentence(){

    }

    fun saveClassifier(classifier: CRFClassifier<CoreMap>){
        classifier.serializeClassifier("path")

    }
}