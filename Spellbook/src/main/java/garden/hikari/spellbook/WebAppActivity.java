package garden.hikari.spellbook;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebView;

import garden.hikari.spellbook.databinding.ActivityWebAppBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WebAppActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView myWebView = (WebView) findViewById(R.id.webview);

        //myWebView.loadUrl("http://www.example.com");
        myWebView.loadData(loadHTMLFileAsString(null), "file/plain", "utf-8");
    }

    public String loadHTMLFileAsString(Uri uri) {
        return "";
    }
    
    // Todo: Have this handle the webstack somehow
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
