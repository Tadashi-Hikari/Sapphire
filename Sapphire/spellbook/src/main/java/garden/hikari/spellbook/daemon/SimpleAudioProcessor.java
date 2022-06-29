package garden.hikari.spellbook.daemon;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

/**
 * The goal for SimpleAudioProcessor is to be an easy way to deploy a binary or STT service
 * from Termux, adb, or another app (might need PendingIntent or BoundService for that)
 *
 * it is designed to handle all configuration and deployment to audioProcessHandler.
 * I made it so it would be simple for me to interact with
 *
 * I was going for having this launch the thread, but the thread handler would be what I
 * reference if I need access to the thread. This part exists as an intent accessible abstraction
 * for reading AudioThreadHandler info (such as from Termux)
 *
 * This is an audio specific Core. Intermediate level code is ok here.
 *
 *
 */

public class SimpleAudioProcessor extends Service {

    AudioProcessHandler handler = null;

    // I think NEW, REPLACE, and CONFIGURE can probably all become a single action
    public static String NEW = "action.sapphire.microphone.NEW";
    public static String STOP = "action.sapphire.microphone.STOP";
    public static String START = "action.sapphire.microphone.START";
    public static String CONFIGURE = "action.sapphire.microphone.CONFIGURE";
    public static String RESUME = "action.sapphire.microphone.RESUME";
    public static String REPLACE = "action.sapphire.microphone.REPLACE";
    public static String REQUEST_DATA = "action.sapphire.microphone.REQUEST_DATA";

    @Override
    public void onCreate() {
        super.onCreate();

        handler = AudioProcessHandler.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        //String phonemeID = intent.getStringExtra("phonemeID");

        try {
            // I think for now, I will just expect to pass a new config JSON rather than individual
            if (action.equals(NEW)) {
                // I can call a new default this way. Configure handles new OR otherwise?
                AudioProcessor processor = AudioConfiguration.getDefaultProcessor(this,intent.getStringExtra(AudioProcessor.TYPE));
                Uri uri = intent.getData();
                if(uri != null) {
                    // This should be moved to a more central location
                    ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "w");
                    processor.streamFromNamedPipe(parcelFileDescriptor.getFileDescriptor());
                }
                AudioProcessHandler.launchThread(processor);
            } else if (action.equals(STOP)) {
                if(intent.hasExtra(AudioConfiguration.PROCESSOR_ID)) {
                    int id = 10; // this is a placeholder id
                    AudioProcessHandler.stopThread(id);
                }else if(intent.hasExtra(AudioConfiguration.CATEGORY)){
                    AudioProcessHandler.stopThreadByCategory(intent.getStringExtra(AudioConfiguration.CATEGORY));
                }
            } else if (action.equals(REQUEST_DATA)) {
                // This puts the stack where requested
                //DataManager.RESULT;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    // This launches it in its own process. Do i need to make a change to how it interacts w/ wakeword, etc
    // This is a fairly low level process for the router core.
    public void launchProcessor(Intent intent) throws Exception {
        String config = intent.getStringExtra(AudioConfiguration.CONFIG_STRING);
        AudioProcessor phonemeProcessor = AudioConfiguration.processConfiguration(config);
        Thread thread = new Thread(phonemeProcessor);
        thread.start();
        // I should probably save this
        long id = thread.getId();
    }

    // This is if I send an intent requesting the ACTIVE instance of one of the three/etc
    // I can just pass configs otherwise?
    public void getAudioProcessorInstance(String type){

    }

    public void validateType() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
