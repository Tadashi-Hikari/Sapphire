package net.carrolltech.athena.sapphire_core;

/*
 This object handles the state of the assistant, and acts as its current
 consciousness. It is for the current moment. For referencing the past, look
 at the CoreMemoryThread

 I think that i am going to make everything in this object static, so that it can be referenced across all core modules
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.carrolltech.athena.sapphire_core.utilityObjects.Pipeline;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

// Is this just holding variables for me?
// Should I make this a stateful object? Does that run the risk of confusion?

public class CoreState {

    public static Boolean started = false;
    public static Boolean initialized = false;
    // This seems like it could be hella dangerous
    public static Context context = null;

    // IDs relates to where some data is in the processing pipeline
    public static Map<Integer, Pipeline> activeIds = new HashMap<>();
    // Intents yet to be processed
    public static LinkedList<Intent> taskQueue = new LinkedList<Intent>();
    // Keep track of running background tasks, to save battery features
    public static LinkedList<String> backgroundTasks = new LinkedList<String>();
    // The list of existing PendingIntents
    public static Map<String,PendingIntent> pendingIntentLedger = new Hashtable<>();

    public static Pipeline updateActiveId(int id){
        Pipeline pipeline = CoreState.activeIds.get(id);
        pipeline.Next();
        // Replace, so the activeId tracker is up to date. I don't like how this is done
        CoreState.activeIds.replace(id,pipeline);
        return pipeline;
    }
}
