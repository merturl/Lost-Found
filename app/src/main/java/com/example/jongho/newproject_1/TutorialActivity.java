package com.example.jongho.newproject_1;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class ToturialActivity extends AppCompatActivity {

    private WebView myWebView;
    private FrameLayout customViewContainer;
    private View videoCustomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toturial);

        myWebView = findViewById(R.id.myWebView);
        customViewContainer = findViewById(R.id.customView_frame);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        myWebView.setWebChromeClient(new CustomWebChromeClient());
        myWebView.loadUrl("https://youtu.be/6b7-Npk56Ds");
    }

    class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (videoCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            final FrameLayout frame = ((FrameLayout) view);
            final View v1 = frame.getChildAt(0);
            view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            v1.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        onHideCustomView();
                        return true;
                    }
                    return false;
                }
            });
            videoCustomView = view;
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.setBackgroundColor(Color.BLACK);
            customViewContainer.bringToFront();
            myWebView.setVisibility(View.GONE);
            customViewContainer.addView(videoCustomView);
        }
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            customViewContainer.removeView(videoCustomView);
            videoCustomView = null;
            customViewContainer.setVisibility(View.INVISIBLE);
            myWebView.setVisibility(View.VISIBLE);
        }
    }
}
