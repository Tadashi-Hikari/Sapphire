package accessibility.stt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.spellbook.daemon.DataManager;
import garden.hikari.spellbook.ui.MainActivity;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

/**
 * This is the callable STT processor. I could see reason to want to
 * run some in parallel, so I am going to make in an object that can
 * easily create multiple instances. However, I can also see needing
 * to switch contexts, so I am going to prepare for that as well.
 *
 * This should be fairly agnostic to the audio input stream when it comes to Vosk
 * How do I wrap something like Precise?
 *
 * I want this to be a Java object, so that I can implement it in ANY service.
 *
 * There will be ONE processor per thread/function. What is batter impact?
 *
 */
public class AudioProcessor extends Service implements Runnable {

    // I should probably pass a way to name this
    class StreamListener implements RecognitionListener {

        private String notificatinType = "";

        public StreamListener(){}
        public StreamListener(String notification){
            notificatinType = notification;
        }

        // This might actually be the bulk processing portion for a model, per recognizer. This could be the expense
        // Spawning new recognizers might be cheaper
        public void onResult(String hypothesis){
            dataManager.push(DataManager.RESULT,hypothesis);
            if(notificatinType.equals(DataManager.RESULT)){
                broadcastHypothesis(hypothesis);
            }
            Log.v("AudioProcessor",hypothesis);
        }
        public void onPartialResult(String hypothesis){
            dataManager.push(DataManager.PARTIAL_RESULT,hypothesis);
            if(notificatinType.equals(DataManager.PARTIAL_RESULT)){
                broadcastHypothesis(hypothesis);
            }
            Log.v("AudioProcessor",hypothesis);
        }

        // Use the boolean as a flag to say we're done here
        public void onFinalResult(String hypothesis){
            dataManager.push(DataManager.FINAL_RESULT,hypothesis);
            if(notificatinType.equals(DataManager.FINAL_RESULT)){
                broadcastHypothesis(hypothesis);
            }
            Log.v("AudioProcessor",hypothesis);
        }
        @Override
        public void onError(Exception exception) {
            Log.e("AudioProcessor","The processor had an error");
        }
        public void onTimeout(){
            Log.i("AudioProcessor","The processor timed out");
        }

        // We do a broadcast, so the app can unsubscribe when it's not visible
        public void broadcastHypothesis(String hypothesis){
            Intent broadcast = new Intent();
            broadcast.setClass(getApplicationContext(), MainActivity.class);
            sendBroadcast(broadcast);
        }
    }

    public static String TYPE = "declare_processor_type";
    public static String WAKE = "processor_type_wake_word";
    public static String GENERAL = "processor_type_general";
    public static String TRANSCRIBE = "processor_type_transcribe";

    private boolean finished = false;
    private float frequency = 0.0f;
    private String name = null;
    private Model model = null;
    private Recognizer rec = null;
    private String grammar = "";
    private String category = null;
    private int id = 0;
    private Uri uri = null;
    private String command = "";

    DataManager dataManager = new DataManager();

    // This is just reading from a 'defaults' settings list. See if I can wrap the
    // other constructor in to it
    public AudioProcessor(){
        frequency = 16000f;
        name = "default";
        rec = null;
        // This may or may not be needed
        id = 0;
    }

    @Override
    public void run() {
        // Either streamNamedPipe, or
    }

    public void setGrammar(String grammar){

        Recognizer rec = new Recognizer(model, 16000.f, "[\"one zero zero zero one\", " +
                "\"oh zero one two three four five six seven eight nine\", \"[unk]\"]");
    }

    public void setModel(Context context){
        StorageService.unpack(context, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                },
                null);
    }

    public void setFrequency(float freq){
        frequency = freq;
    }

    public void setName(String n){
        name = n;
    }

    public void setCategory(String cat){
        category = cat;
    }

    public void setShellCommand(String cmd) { command = cmd; } // Do something

    public Recognizer getRecognizer(){
        return rec;
    }

    // I think this is handling some Android backend stuff
    public void streamMicrophone() throws Exception{
        SpeechService speechService = new SpeechService(rec, 16000.0f);
        speechService.startListening(new StreamListener());
    }

    // This is to pass android microphone data TO the named pipe
    public void streamToNamedPipe(FileDescriptor fileDescriptor) throws Exception{
        // Should the audio be processed (cleaned up) or not
        MediaRecorder mediaRecorder = new MediaRecorder();
        // Might need to make this Raw
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFile(fileDescriptor);
        mediaRecorder.start();
    }

    // This is used for Vosk to read from a file for transcription
    public void streamFromFile(FileDescriptor fileDescriptor) throws Exception{
        InputStream ais = new FileInputStream(fileDescriptor);

        // I can just use this for passing between any app. Termux plugin will handle Termux
        FileDescriptor fd = new FileDescriptor();
        ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(fd);

        if (ais.skip(44) != 44) throw new IOException("File too short");
        SpeechStreamService speechStreamService = new SpeechStreamService(rec, ais, 16000);
        speechStreamService.start(new StreamListener());
    }

    public void wrapBinary(){
        // pass the namedPipe to stdin
        // This doesn't handle ANY functionality, other than just passing the audio data along
    }

    public boolean isFinished(){
        return finished;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
