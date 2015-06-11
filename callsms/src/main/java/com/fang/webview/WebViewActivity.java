package com.fang.webview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.common.controls.CustomWebView;
import com.fang.common.util.StringUtil;
import com.fang.datatype.ExtraName;


/**
 * Created by benren.fj on 6/11/15.
 */
public class WebViewActivity extends Activity {

    private CustomWebView mWebView;
    private TextView mTitleTV;
    private ProgressBar mProgressBar;
    private ImageView mBack;

    private View myView;
    private WebChromeClient.CustomViewCallback myCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);
        mWebView = (CustomWebView) findViewById(R.id.webview);
        mTitleTV = (TextView) findViewById(R.id.title);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mBack = (ImageView) findViewById(R.id.back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initWebView();

        open(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        open(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);

        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
    }

    private void open(Intent intent) {
        String url = intent.getStringExtra(ExtraName.URL);
        if (StringUtil.isEmpty(url)) {
            return;
        }
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (!hiddenVideoView()) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    }
    private class MyWebChromeClient extends WebChromeClient{

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            hiddenVideoView();
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);

            if (myCallback != null) {
                myCallback.onCustomViewHidden();
                myCallback = null;
                return;
            }

            ViewGroup parent = (ViewGroup) mWebView.getParent();
            parent.removeView(mWebView);
            parent.addView(view);
            myView = view;
            myCallback = callback;

        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if(newProgress < 100){
                mProgressBar.setVisibility(View.VISIBLE);
            }

            if(newProgress == 100){
                mProgressBar.setVisibility(View.GONE);
            }
            mProgressBar.setProgress(newProgress);
            mProgressBar.postInvalidate();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mTitleTV.setText(title);
        }
    }

    public boolean hiddenVideoView(){
        if (myView != null) {

            if (myCallback != null) {
                myCallback.onCustomViewHidden();
                myCallback = null ;
            }

            ViewGroup parent = (ViewGroup) myView.getParent();
            parent.removeView( myView);
            parent.addView(mWebView);

            myView = null;
            return true;
        }
        return false;
    }
}
