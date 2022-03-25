package net.carrolltech.athena.essence.Body;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.SystemClock;

import java.util.Random;

public class Utilities {


    // This should all be moved into SapphireFrameworkService()
    public static String CONFIG = "sample-core-config.conf";
    //public static String DEFAULT_MODULES_TABLE = "defaultmodules.tbl";
    public static String STARTUP_TABLE = "background.tbl";
    public static String PIPELINE_TABLE = "pipelinetable.tbl";
    public static String ALIAS_TABLE = "alias.tbl";

    public static Integer generateId(){
        // I should probably do a check to make sure this is unique
        return new Random().nextInt();
    }

    // This is used for the FIRST runtime start of an app, to get the PendingIntent
    public void requestPendingIntent(ServiceConnection connection, Intent intent){
        intent.setAction("REQUEST_PENDING_INTENT"); // Placeholder string, so binding can still be used for other purposes
        State.getContext().bindService(intent,connection, BIND_AUTO_CREATE);
        // I just need enough time for the service to init, and be non-background
        SystemClock.sleep(700);
        State.getContext().startService(intent);
    }
}
