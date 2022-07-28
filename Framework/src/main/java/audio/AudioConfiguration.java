package audio;

/**
 * This is really just a helper class for configuring the phoneme processor
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AudioConfiguration {
    public static String CONFIG_STRING = "json_config_string";
    public static String DEFAULT = "vosk_default_processor";
    // Vosk specific. This will be added by URI
    public static String MODEL_URI = "vosk_model_uri";
    public static String GRAMMAR = "vosk_grammar";
    public static String NOTIFICATION = "vosk_notification_type";
    // Audio specific
    public static String NAME = "audio_name";
    public static String FREQUENCY = "audio_frequency";
    public static String PROCESSOR_ID = "audio_processor_id";
    public static String CATEGORY = "audio_processor_category";
    // This might be Termux specific, but it's for running binaries
    public static String WRAP_COMMAND = "wrap_command";
    // If you want to be notified of updates w/ and intent
    // intent.putExtra("notification","class/pkg");
    // intent.putExtra("notification_type","partial,result,final");
    // If you want to use a broadcast receiver
    // intent.putExtra("broadcast","details");

    public static AudioProcessor processConfiguration(String configString) throws Exception {
        JSONObject config = (JSONObject) new JSONTokener(configString).nextValue();

        AudioProcessor processor = new AudioProcessor();
        if (config.has(WRAP_COMMAND)) processor.setShellCommand(WRAP_COMMAND);
        if (config.has(NAME)) processor.setName(config.getString(NAME));
        if (config.has(FREQUENCY)) processor.setName(config.getString(FREQUENCY));
        if (config.has(GRAMMAR)) processor.setGrammar(config.getString(GRAMMAR));
        if (config.has(CATEGORY)) processor.setCategory(CATEGORY);

        return processor;
    }

    public static AudioProcessor getDefaultProcessor(Context context, String type) throws Exception{
        AssetManager assets = context.getAssets();
        String configString = "";

        if(type.equals(AudioProcessor.WAKE)){
            // read the contents of the json file
            InputStreamReader inputStreamReader = new InputStreamReader(assets.open("general.json"));
            BufferedReader bufferedReader  = new BufferedReader(inputStreamReader);
            StringBuilder configStringBuilder = new StringBuilder();
            bufferedReader.lines().forEach(configStringBuilder::append);
            configString = configStringBuilder.toString();
            Log.v("AudioConfiguration",configString);
        }else if(type.equals(AudioProcessor.GENERAL)){

        }else if(type.equals(AudioProcessor.TRANSCRIBE)){
            assets.open("transcribe.json");
        }

        AudioProcessor processor = processConfiguration(configString);
        // This is likely where the issue stems from. I am setting the model from a context outside of the service context
        //processor.setModel();

        return processor;
    }

    public Boolean checkCategory(String threadCategory){
        // One live, one constant, one post-recording
        if(threadCategory.equals("WakeWord,General,Transcription")){
            // if the category is in use
            return false;
        }
        // if the category is available
        return true;
    }
}
