package net.carrolltech.athena.sapphire_core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.RequiresApi;

/*
 This is a lightweight service that handles three things. 1) handling the init and lifecycle of the core,
 2) holding volatile state variables (via the CoreState static object) and 3) displaying the user notification
 */

public class CoreDaemon extends Service {

    // This is persistent memory
    CorePersistentMemory memory = null;
    // State should maintain itself in a File, in case the application is killed
    CoreState state = null;
    CoreInterpreter interpreter = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        startNotification();

        // Should I run all of these in StateThread?
        // This is the initialization
        interpreter = new CoreInterpreter();
        memory = new CorePersistentMemory();
        // This seems suuuuper dangerous. I'm setting the variable, but anyone can modify it
        CoreState.context = this.getBaseContext();

        // I could probably wait to start these services
        // Also, I can pause them when the user hasn't used the assistant
        interpreter.run();
        // Vosk and MozillaTTS are started by the Android system

        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startNotification(){
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("Sapphire","Sapphire", importance);
        channel.setDescription("Sapphire Assistant");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    //The goal for onStartCommand is to immediately pass on the information to the CoreInterpreter
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Update the queue for the router
        // There seems to be contention over whether the queue is router or state.
        try {
            // This.... shouldnt be here?
            //CoreState.pendingIntentLedger.add(intent.getParcelableExtra("assistant.framework.module.protocol.PENDING_INTENT"));
            CoreState.taskQueue.add(intent);
        }catch (Exception e){
            System.out.println("--------------------------------------------------------------");
            e.printStackTrace();
            System.out.println("--------------------------------------------------------------");
            System.out.println("It's likely that somehow onStartCommand was triggered without an Intent");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
    class Connection implements ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        public void onBindingDied(ComponentName name) {
            //Update the log
        }

        public void onServiceDisconnected(ComponentName name) {
            // Update the log
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
