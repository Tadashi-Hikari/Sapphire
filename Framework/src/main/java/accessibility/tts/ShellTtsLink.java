package accessibility.tts;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;

public class ShellTtsLink extends Service {
    /**
     * Todo: Implement Pipe in, and pipe out.
     *
     * I would expect Termux would request a way to send information here. I suppose
     * requesting an audio stream from an external source and passing the Uri would
     * make the most sense
     */

    /**
     *  onShellIntent *would* usually be onTermuxIntent, but I wanted it to be a little more generic
     *  than Termux, just in case.
     */
    public void onShellIntent(Intent inboundIntent) throws FileNotFoundException {
        // Todo: wrap these steps in a convenience function
        Uri uri = inboundIntent.getData();
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        // Todo: I actually want this to be an inputStream so I can treat it as a pipe, but this will do for now
        FileWriter fileWriter = new FileWriter(parcelFileDescriptor.getFileDescriptor());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
