package com.xlzhen.dlnavideodownload;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.webkit.JavascriptInterface;
import android.view.View;

public class VideoPlayerActivity extends AppCompatActivity {

    private WebView webView;
    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        View mainView = findViewById(R.id.web_view);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        webView = findViewById(R.id.web_view);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void onStateChanged(boolean playing) {
                runOnUiThread(() -> {
                    btnBack.setVisibility(playing ? View.GONE : View.VISIBLE);
                });
            }
        }, "Android");

        final String videoPath = getIntent().getStringExtra("video_path");
        
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("https://localvideo/")) {
                    try {
                        File file = new File(videoPath);
                        FileInputStream fis = new FileInputStream(file);
                        return new WebResourceResponse("video/mp4", "UTF-8", fis);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        if (videoPath != null) {
            String html = "<html><body style='margin:0;padding:0;background:black;display:flex;justify-content:center;align-items:center;height:100vh;'>" +
                    "<video id='v' width='100%' height='auto' controls autoplay style='max-height:100%;'>" +
                    "<source src='https://localvideo/video.mp4' type='video/mp4'>" +
                    "Your browser does not support the video tag." +
                    "</video>" +
                    "<script>" +
                    "var v = document.getElementById('v');" +
                    "v.onplay = function() { Android.onStateChanged(true); };" +
                    "v.onpause = function() { Android.onStateChanged(false); };" +
                    "v.onended = function() { Android.onStateChanged(false); };" +
                    "</script>" +
                    "</body></html>";
            webView.loadDataWithBaseURL("https://localvideo/", html, "text/html", "UTF-8", null);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}