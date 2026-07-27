package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

// Full-screen WebView that loads the PesaPal payment page.
// When the user completes (or cancels) payment, PesaPal redirects to our
// /api/pesapal/callback endpoint. We detect that URL and close with RESULT_OK.
public class PesaPalWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_REDIRECT_URL = "redirect_url";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("/api/pesapal/callback")) {
                    setResult(Activity.RESULT_OK);
                    finish();
                    return true;
                }
                return false;
            }
        });

        String redirectUrl = getIntent().getStringExtra(EXTRA_REDIRECT_URL);
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            webView.loadUrl(redirectUrl);
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            setResult(Activity.RESULT_CANCELED);
            super.onBackPressed();
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
