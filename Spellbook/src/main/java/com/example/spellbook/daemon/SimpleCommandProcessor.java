package com.example.spellbook.daemon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * The goal of SimpleCommandProcessor is to be the core routing service. It services
 * more than just the audio component, and it is meant to be simple for about 6-7 total actions
 */

public class SimpleCommandProcessor extends Service {

    public static String PROCESSOR_ACTION = "action.spellbook.command.PROCESSOR";
    public static String NOTIFICATION_ACTION = "action.spellbook.command.NOTIFICATION";
    public static String INTEGRATION_ACTION = "action.spellbook.command.INTEGRATION";
    public static String COMPLETED_ACTION = "action.spellbook.command.COMPLETED";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            String action = intent.getAction();

            // These are the modes it can start in
            if(action.equals(NOTIFICATION_ACTION)) {
                toggleNotification();
            }else if(action.equals(PROCESSOR_ACTION)){
                launchTermuxProcessor(intent);
                //launchProcessor(intent);
            }else if(action.equals(INTEGRATION_ACTION)){
                integrate();
            }else if(action.equals(COMPLETED_ACTION)){
                // Send out a TermuxIntent to run a script
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /** Pass the intent along to the SimpleAudioProcessor service. Im considering making it simply
     * and object, or embracing its function as a service and refining its functionality
     *
     * @param intent
     */
    public void launchProcessor(Intent intent){
        Intent audioProcessorIntent = intent;
        // Todo: swap placeholder with valid and cogent code
        audioProcessorIntent.setClassName(this, "SimpleAudioProcessor.class");
        startService(audioProcessorIntent);
    }

    // This is the placeholder to see if I can wrap precise
    public void launchTermuxProcessor(Intent intent){
        // Todo: swap placeholder with valid and cogent code
        //ScriptLauncher.runTermuxCommand(this,new String[]{"bash audio-pipe.sh"});
    }

    public void toggleNotification(){}
    public void integrate(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

