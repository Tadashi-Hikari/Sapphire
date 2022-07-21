package ui;

/*
This is the simplest service for requesting a recognizer, and using it for either wake word, general,
or transcribe mode
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.FileDescriptor;

import com.example.spellbook.R;

/*
RequestRecognizer would come from phonemeProcessor. This is essential for Sapphire. Maybe not spellbook
 */

public class SimpleForegroundService extends Service {
    String notification_title = "test";
    String notification_message = "testing";
    String ticker_text = "ticker";
    int ONGOING_NOTIFICATION_ID = 2;

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Spellbook Listener";
            String description = "This shows that Spellbook is listening for commands";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("spellbook",name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void foregroundService(){
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"spellbook")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Semper Liber")
                .setContentText("A Republic, if you can keep it -Benjamin Franklin");

        Notification notification = builder.build();

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if(action.equals("start_notification")){
            foregroundService();
        }else if(action.equals("stop_notification")){

        }else if(action.equals("update_notification")){
            // I might not need this, depending on how Android handles the logic
        }
        FileDescriptor fd = new FileDescriptor();
        //ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(fd);
        //getContentResolver()

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
