package com.example.framework;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/*
This is the service that exists to handle foreground notifications and the like
That means this likely has to bind the other internal services.

This should be as lightweight as possible if it's always running. It should probably be its own
process
 */

public class CentralNotificationService extends Service {
    public CentralNotificationService() {
    }

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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Semper Liber")
                .setContentText("A Republic, if you can keep it -Benjamin Franklin")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = builder.build();


// Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String action = intent.getAction();

            if (action.equals("toggle")) {
                stopForeground(ONGOING_NOTIFICATION_ID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void bindList(){
        String list = "wakeword,general,transcribe,pulseaudio,spellbook,etc";
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}