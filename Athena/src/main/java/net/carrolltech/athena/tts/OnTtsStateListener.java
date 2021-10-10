package net.carrolltech.athena.tts;

/**
 * @author {@link "mailto:xuefeng.ding@outlook.com" "Xuefeng Ding"}
 * Created 2020-07-28 14:25
 */
public interface OnTtsStateListener {
    public void onTtsReady();

    public void onTtsStart(String text);

    public void onTtsStop();
}