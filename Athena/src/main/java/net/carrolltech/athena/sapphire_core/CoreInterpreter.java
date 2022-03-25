package net.carrolltech.athena.sapphire_core;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import net.carrolltech.athena.sapphire_core.utilityObjects.CoreUtilities;
import net.carrolltech.athena.sapphire_core.utilityObjects.Pipeline;

/*
For PendingIntents Core should bind and unbind/kill the process, in order to make sure
nothing is running down the users battery
 */

public class CoreInterpreter implements Runnable {

    @Override
    public void run(){
        while(true){
            if(!CoreState.taskQueue.isEmpty()){
                process(CoreState.taskQueue.remove());
            }else{
                try {
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void takeNextStep(Intent intent){
        Pipeline pipeline = CoreState.updateActiveId(intent.getIntExtra("ID",-1));
        intent.setClassName(pipeline.getPackageName(),pipeline.getClassName());
        if(CoreState.pendingIntentLedger.containsKey(pipeline.getPackageName()+";"+pipeline.getClassName())){
            doBackgroundTask(intent);
        }else{ // I should check the possible Plug-Ins to see if I need to request a new PendingIntent
            doForegroundTask(intent);
        }
    }

    public void process(Intent intent) {
        if (CoreState.initialized) {
            if(intent.getStringExtra("ID").isEmpty()) { // This is done for things already underway
                int id = CoreUtilities.generateId();
                intent.putExtra("ID",id);
            }else{
                takeNextStep(intent);
            }

            // This is pretty straight forward. The stdin app is telling us what it wants to do
            if(intent.getAction() != null){
                // This is a fluid way. The action name maps to a plug-in
                if(CorePersistentMemory.pipelines.containsKey(intent.getAction())){
                    System.out.println("Matched");
                    Pipeline pipeline = CorePersistentMemory.pipelines.get(intent.getAction());
                    intent.setAction(null); // Clear it out, so it doesn't get triggered again
                    takeNextStep(intent);
                }else{
                    // The needed app simply might not have been registered in this session yet
                    // doScanForPlugIn()
                }
            // No ID (meaning it's new/unk data) and no Action means we have to guess
            }else if(intent.hasExtra("PendingIntent")){
                CoreState.pendingIntentLedger.put(intent.getStringExtra("NAME"),intent.getParcelableExtra("PendingIntent"));
                doBackgroundTask(intent);
            }else if(!intent.getStringExtra("FROM").isEmpty()) { // This is the final error recovery, to see if we know how to handle the module
                // Load the whole route info for this new intent. ID was assigned above
                Pipeline newPipeline = CorePersistentMemory.pipelines.get(intent.getStringExtra("FROM"));
                CoreState.activeIds.put(intent.getIntExtra("ID",-1), newPipeline);
                intent.setClassName(newPipeline.getPackageName(), newPipeline.getClassName());
                CoreState.context.startService(intent);
            }else if(intent.getType().equals("MIME")){ // Pseudocode. This is an error recovery to guess how to handle new data
                Log.v("Core Interpreter","MIME Type matching not yet implemented");
            }else{
                Log.e("Core Interpreter","Input denied, unknown how to handle data");
            }
        }else{ // If it's not initialized, wait until it has been, then empty the queue
            while (!CoreState.initialized) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Error sleeping");
                }
            }
        }

        if(!CoreState.taskQueue.isEmpty()){ // Run through the queue if the system got backed up
            process(CoreState.taskQueue.remove()); // Pop the top of the stack
        }
    }

    // Background tasks deal with PendingIntents
    public void doBackgroundTask(Intent intent){
        PendingIntent pendingIntent = CoreState.pendingIntentLedger.get(1);
        try {
            pendingIntent.send(CoreState.context, 13, intent); // That 13 doesn't mean anything. It's just an ID code for Android
        }catch(Exception e){
            e.printStackTrace();
            Log.e("Interpreter Thread","The context doesn't exist!");
        }
    }

    // Foreground tasks deal with Activities
    public void doForegroundTask(Intent intent){
        CoreState.context.startActivity(intent);
    }
}
