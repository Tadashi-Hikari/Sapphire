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
import java.net.DatagramSocket;
import java.net.SocketException;

public class ShellTtsLink extends Service {
    /**
     * Todo: Implement Pipe in, and pipe out.
     *   I should have it be a tempFile (so it's in ram) that I pass TO Termux, if possible
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
        // I am going replace this with a socket, because it makes more sense
        FileWriter fileWriter = new FileWriter(parcelFileDescriptor.getFileDescriptor());
    }

    // This will need an audio in/audio out
    public void connectAudioSocket() throws SocketException {
        // This is a placeholder port for now
        int audioPort = 9999;
        DatagramSocket socket = new DatagramSocket(audioPort);
    }
    public void disconnectAudioSocket(){}

    public void audioSocketIn(){}
    public void audioSocketOut(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
