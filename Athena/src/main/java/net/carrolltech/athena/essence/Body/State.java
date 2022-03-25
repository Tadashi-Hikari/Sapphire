package net.carrolltech.athena.essence.Body;

/**
 *  This singleton object handles the state of the assistant, and acts as its current
 *  consciousness. It is for the current moment. For referencing the past, look
 *  at the Memory object.
 *
 *  State has been forced on us by Android, at a minimum to handle if the UI is visible and drawn.
 *  However, state can also be used to reference things that CANNOT be saved, such as PendingIntents.
 *  It is best to keep track of other living services here, as opposed to settings and configs, which
 *  should be held in Memory.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class State{
    private static Boolean started = false;
    private static Boolean initialized = false;
    private static Context applicationContext = null;

    // IDs relates to where some data is in the processing pipeline
    public static Map<Integer, Pipeline> activeIds = null;
    // Intents yet to be processed
    public static LinkedList<Intent> taskQueue = null;
    // Keep track of running background tasks, to save battery features
    public static LinkedList<String> backgroundTasks = null;
    // The list of existing PendingIntents
    public static Map<String,PendingIntent> pendingIntentLedger = null;

    private static State state = null;

    private State(){
        started = true;
        activeIds = new HashMap<>();
        taskQueue = new LinkedList<Intent>();
        backgroundTasks = new LinkedList<String>();
        pendingIntentLedger = new Hashtable<>();
        /*
        Since Sapphire runs in the background, it's only worried about the state of the app and app
        resources, not the state of an Activity or anything.
         */
        applicationContext = getContext().getApplicationContext();
        initialized = true;
    }

    // This is for getting a reference to this singleton
    public static State getReference(){
        if(state == null){
            state = new State();
        }

        return state;
    }

    public static Context getContext(){
        return applicationContext;
    }

    public static Boolean isInitialized(){
        return initialized;
    }

    public static void validateState(){
        // I guess I should check to make sure things are in a valid state
    }

    // updateActiveId is returning the next item in the pipeline, updating the tracker
    public static Pipeline updateActiveId(int id){
        Pipeline pipeline = State.activeIds.get(id);
        pipeline.next();
        // Replace, so the activeId tracker is up to date. I don't like how this is done
        State.activeIds.replace(id,pipeline);
        return pipeline;
    }
}
