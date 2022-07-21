package utility;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This is designed to handle the creation and distruction of a foreground service for any of my
 * derivative apps that might rely on a foreground service. The idea here is that all of my apps
 * could request this same service, if found on device, in order to create a nice, unified user experience
 */

public class ForegroundServiceManager extends Service {
    public ForegroundServiceManager() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}