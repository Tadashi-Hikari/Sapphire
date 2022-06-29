package garden.hikari.spellbook.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;

import garden.hikari.spellbook.daemon.AudioProcessor;
import garden.hikari.spellbook.daemon.AudioProcessHandler;

import java.util.ArrayList;

import garden.hikari.spellbook.R;
import garden.hikari.spellbook.daemon.SimpleAudioProcessor;
import garden.hikari.spellbook.helpers.ScriptLauncher;

public class MainActivity extends AppCompatActivity {

    public final int REQUEST_CODE = 5;

    private class UIBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new UIBroadcastReceiver(), null);
    }

    // This will be for a button, but whatever.
    public void updateUI() {
        Button button = findViewById(R.id.view_tree_view_model_store_owner);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(new UIBroadcastReceiver(), null);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(new UIBroadcastReceiver());
    }

    public void openDirectory(Uri uriToLoad) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

        startActivityForResult(intent, REQUEST_CODE);
    }

    @SuppressLint("WrongConstant")
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

                // Save it so it can be accessed later
                SharedPreferences preferences = this.getSharedPreferences("spellbook",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("directory",uri.toString());
            }
        }
    }

    //----------------UI Logic--------------

    //----------------Button Logic--------------
    // This should pull from a default value, or an EditText box, whichever is newer
    public void launchScript(View view){
        // See ScriptLauncher
        // if proot, or Termux
        if(true){
            ScriptLauncher.runTermuxCommand(this,new String[]{"something"});
        }else{
            ScriptLauncher.runProotCommand(this,new String[]{"something"});
        }
    }
    public void toggleForegroundService(View view){
        // See SimpleForegroundService
        Intent intent = new Intent();
        // This needs to check if the notification exists
        if(true){
            intent.setAction("start_notification");
        }else{
            // Shut down all running audio threads
            AudioProcessHandler handler = AudioProcessHandler.getInstance();
            ArrayList<String> active = handler.getRunningAudioThreads();
            for(String audioThread: active){
                // Take the thread name, category, or direct thread, and shut it down
                handler.stopThreadByCategory(audioThread);
            }
            intent.setAction("stop_notification");
        }
        intent.setClassName(this,SimpleForegroundService.class.getName());
    }
    public void launchProcessor(View view){
        // See MicrophoneHandler
        Intent commandIntent = new Intent();
        commandIntent.setAction(SimpleAudioProcessor.NEW);
        commandIntent.putExtra(AudioProcessor.TYPE,AudioProcessor.WAKE);
        startService(commandIntent);
    }
    public void transcribe(View view){
        // See MicrophoneHandler
        Intent commandIntent = new Intent();
        commandIntent.setAction(SimpleAudioProcessor.NEW);
        commandIntent.putExtra(AudioProcessor.TYPE,AudioProcessor.TRANSCRIBE);
        startService(commandIntent);
    }
    public void export(View view){
        // What am I exporting?
    }
    public void viewInstructions(View view){
        // Launch an intent/activity to redirect
        // Scrape from website
    }
}