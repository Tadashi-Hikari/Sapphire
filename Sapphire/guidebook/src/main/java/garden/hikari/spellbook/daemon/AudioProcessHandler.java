package garden.hikari.spellbook.daemon;

import java.util.ArrayList;

/**
 * This is backend stuff. No reason for this to be higher level logic for right now
 *
 * The idea here is it's a memory reference for what is running. It does not itself need to
 * be a service or anything.
 */

public class AudioProcessHandler {
    // This should probably be a dictionary or the like
    static Thread wakeword = null;
    static Thread general = null;
    static Thread transcription = null;

    private static AudioProcessHandler threadHandler = null;

    private AudioProcessHandler(){}

    // I only want one instance of this in my program. It should probably be its own process tbh
    public static AudioProcessHandler getInstance(){
        if(threadHandler == null){
            threadHandler = new AudioProcessHandler();
        }
        return threadHandler;
    }

    public void launchDefaultThread(String type) throws Exception{
        AudioProcessor processor = AudioConfiguration.processConfiguration(type);
        launchThread(processor);
    }

    public static void launchThread(Runnable processor){
        Thread thread = new Thread(processor);
    }

    public static void stopThread(int id){
        //thread.stop();
    }
    public void resumeThread(Thread thread){
        thread.resume();
    }
    public static void stopThreadByCategory(String threadCategory){ wakeword.stop(); }
    public static void resumeThreadByCategory(String threadCategory){
        wakeword.resume();
    }
    public ArrayList<String> getRunningAudioThreads(){
        return null;
    }

    // This would be the reconfiguring
    public void replaceThread(String threadCategory) throws Exception{
        if(!validateAuthorization()){
            return;
        }else{
            stopThreadByCategory(threadCategory);
        }

        AudioProcessor processor = AudioConfiguration.processConfiguration("{conf}");
        // wakeword, general, or transcription
        wakeword = new Thread(processor);
    }

    public Boolean validateAuthorization(){
        return null;
    }
}
