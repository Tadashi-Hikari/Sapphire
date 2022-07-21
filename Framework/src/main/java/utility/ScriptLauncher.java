package utility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ScriptLauncher{
    // This is for running commands that might need a full Linux env
    public static void runProotCommand(Context context, String[] args){
        Intent intent = generateTemplateIntent();
        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/proot");
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args);
        context.startService(intent);
    }

    // This is for commands that run in top level Termux
    public static void runTermuxCommand(Context context, String[] args){
        Intent intent = generateTemplateIntent();
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args);
        context.startService(intent);
    }

    public static Intent generateTemplateIntent(){
        Intent intent = new Intent();
        intent.setClassName("com.termux", "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
        intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0");

        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash");
        return intent;
    }
}
