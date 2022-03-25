package net.carrolltech.athena.sapphire_core.utilityObjects;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.SystemClock;

import net.carrolltech.athena.sapphire_core.CoreState;

import java.util.Random;

public class CoreUtilities {


    // This should all be moved into SapphireFrameworkService()
    public static String CONFIG = "sample-core-config.conf";
    //public static String DEFAULT_MODULES_TABLE = "defaultmodules.tbl";
    public static String STARTUP_TABLE = "background.tbl";
    public static String PIPELINES_TABLE = "pipelinetable.tbl";
    public static String ALIAS_TABLE = "alias.tbl";

    public static Integer generateId(){
        // I should probably do a check to make sure this is unique
        return new Random().nextInt();
    }

    // This is used for the FIRST runtime start of an app, to get the PendingIntent
    public void requestPendingIntent(ServiceConnection connection, Intent intent){
        intent.setAction("REQUEST_PENDING_INTENT"); // Placeholder string, so binding can still be used for other purposes
        CoreState.context.bindService(intent,connection, BIND_AUTO_CREATE);
        // I just need enough time for the service to init, and be non-background
        SystemClock.sleep(700);
        CoreState.context.startService(intent);
    }
}
