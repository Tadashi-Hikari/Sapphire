package net.carrolltech.athena.essence;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import net.carrolltech.athena.essence.Body.Memory;
import net.carrolltech.athena.essence.Body.State;
import net.carrolltech.athena.essence.Body.Utilities;
import net.carrolltech.athena.essence.Body.Pipeline;

/*
Prototype name: Mind
Actual name: CoreInterpreter

For PendingIntents Core should bind and unbind/kill the process, in order to make sure
nothing is running down the users battery
 */

public class Mind implements Runnable {

    @Override
    public void run(){
        while(true){
            if(!State.taskQueue.isEmpty()){
                process(State.taskQueue.remove());
            }else{
                try {
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void process(Intent intent) {
        if (State.isInitialized()) {
            if(intent.getStringExtra("ID").isEmpty()) { // This is done for things already underway
                int id = Utilities.generateId();
                intent.putExtra("ID",id);
            }else{
                doNextStep(intent);
            }

            // This is pretty straight forward. The stdin app is telling us what it wants to do
            if(intent.getAction() != null){
                // This is a fluid way. The action name maps to a plug-in
                if(Memory.pipelines.containsKey(intent.getAction())){
                    System.out.println("Matched");
                    Pipeline pipeline = Memory.pipelines.get(intent.getAction());
                    intent.setAction(null); // Clear it out, so it doesn't get triggered again
                    doNextStep(intent);
                }else{
                    // The needed app simply might not have been registered in this session yet
                    scanForPlugins();
                    // if found, run (update & takeNextStep(intent))
                }
            // No ID (meaning it's new/unk data) and no Action means we have to guess
            }else if(intent.hasExtra("PendingIntent")){ // Having an attached PendingIntent means it needs to be registered.
                State.pendingIntentLedger.put(intent.getStringExtra("NAME"),intent.getParcelableExtra("PendingIntent"));
                // It probably wants to run right away, but what if someone tries to leverage that to inject a background task
            }else if(!intent.getStringExtra("FROM").isEmpty()) { // This is the final error recovery, to see if we know how to handle the module
                // Load the whole route info for this new intent. ID was assigned above
                Pipeline newPipeline = Memory.pipelines.get(intent.getStringExtra("FROM"));
                State.activeIds.put(intent.getIntExtra("ID",-1), newPipeline);
                intent.setClassName(newPipeline.getPackageName(), newPipeline.getClassName());
                State.getContext().startService(intent);
            }else if(intent.getType().equals("MIME")){ // Pseudocode. This is an error recovery to guess how to handle new data
                Log.v("Core Interpreter","MIME Type matching not yet implemented");
            }else{
                Log.e("Core Interpreter","Input denied, unknown how to handle data");
            }
        }else{ // If it's not initialized, wait until it has been, then empty the queue
            int timeCounter = 0;
            while (!State.isInitialized()) {
                try {
                    Thread.sleep(1000);
                    System.out.print("Core initializing. Time counter: "+timeCounter);
                    timeCounter++;
                    // This can be adjusted if need be. Timeout = 30 seconds
                    if(timeCounter >= 30){
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Error sleeping");
                }
            }
        }

        if(!State.taskQueue.isEmpty()){ // Run through the queue if the system got backed up
            process(State.taskQueue.remove()); // Pop the top of the stack
        }
    }

    public void scanForPlugins(){
        // Search for plugin that matches type,data,name,sapphire_feature, etc
    }

    public void doNextStep(Intent intent){
        Pipeline pipeline = State.updateActiveId(intent.getIntExtra("ID",-1));
        intent.setClassName(pipeline.getPackageName(),pipeline.getClassName());
        if(State.pendingIntentLedger.containsKey(pipeline.getPackageName()+";"+pipeline.getClassName())){
            doBackgroundTask(intent);
        }else{ // I should check the possible Plug-Ins to see if I need to request a new PendingIntent
            doForegroundTask(intent);
        }
    }

    // Background tasks deal with PendingIntents
    public void doBackgroundTask(Intent intent){
        PendingIntent pendingIntent = State.pendingIntentLedger.get(1);
        try {
            pendingIntent.send(State.getContext(), 13, intent); // That 13 doesn't mean anything. It's just an ID code for Android
        }catch(Exception e){
            e.printStackTrace();
            Log.e("Interpreter Thread","The context doesn't exist!");
        }
    }

    // Foreground tasks deal with Activities
    public void doForegroundTask(Intent intent){
        State.getContext().startActivity(intent);
    }
}
