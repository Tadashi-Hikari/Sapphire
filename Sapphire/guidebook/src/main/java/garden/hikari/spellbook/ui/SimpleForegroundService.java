package garden.hikari.spellbook.ui;

/*
This is the simplest service for requesting a recognizer, and using it for either wake word, general,
or transcribe mode
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.FileDescriptor;

/*
RequestRecognizer would come from phonemeProcessor. This is essnetial for Sapphire. Maybe not spellbook
 */

public class SimpleForegroundService extends Service {


    int CHANNEL_DEFAULT_IMPORTANCE = 1;
    String notification_title = "test";
    String notification_message = "testing";
    String ticker_text = "ticker";
    int ONGOING_NOTIFICATION_ID = 2;

    public void foregroundService(){
        // If the notification supports a direct reply action, use
// PendingIntent.FLAG_MUTABLE instead.
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(ONGOING_NOTIFICATION_ID))
                .setSmallIcon(com.example.framework.R.drawable.ic_launcher_foreground)
                .setContentTitle("Semper Liber")
                .setContentText("A Republic, if you can keep it -Benjamin Franklin")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = builder.build();

// Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if(action.equals("start_notification")){

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
