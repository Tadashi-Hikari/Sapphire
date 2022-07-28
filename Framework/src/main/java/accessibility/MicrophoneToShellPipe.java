package accessibility;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

public class MicrophoneToShellPipe extends Service {

    // The output here would be the microphone output
    Uri stdoutPipe = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        try {
            if (intent.hasExtra("stdout")) {
                stdoutPipe = intent.getData();
                if (stdoutPipe != null) {
                    // This should be moved to a more central location
                    ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(stdoutPipe, "r");
                    // Feed the microphone data into stdinPipe
                    MediaRecorder mediaRecorder = new MediaRecorder();
                    // Might need to make this Raw
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFile(parcelFileDescriptor.getFileDescriptor());
                    mediaRecorder.start();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
