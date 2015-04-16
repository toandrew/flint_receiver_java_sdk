/**
 * Copyright (C) 2013-2015, The OpenFlint Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.infthink.flintreceiver.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;

/**
 * Use to contain all fallback Flint receivers.
 * 
 * This Activity will use WebView as a container on Android platform( <= 4.2 ?)
 * 
 * It may be very useful in OLD android TV Sets.
 */
public class FlintContainerActivity extends Activity {
    private static final String TAG = "FlintContainerActivity";

    private WebView mWebView;

    BroadcastReceiver mFlintReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.e(TAG, "[Fallback]Ready to call finish!!!");
            finish();
            Log.e(TAG, "[Fallback]End to call finish!!!");
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate!");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.flint_container);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        mWebView = (WebView) findViewById(R.id.container_main);
        mWebView.setWebChromeClient(new WebChromeClient());
        
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(PluginState.ON);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setSupportMultipleWindows(false);

        WebViewClient mWebviewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, "shouldOverrideUrlLoading:url[" + url + "]");
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view,
                    SslErrorHandler handler, SslError error) {
                Log.e(TAG, "onReceivedSslError:"+ error.toString());
                handler.proceed();
            }
            
            @Override
            public void onLoadResource(WebView view, String url) {
                Log.e(TAG, "onLoadResource: " + url);
            }
        };
        mWebView.setWebViewClient(mWebviewClient);

        IntentFilter filter = new IntentFilter("fling.action.stop_receiver");
        registerReceiver(mFlintReceiver, filter);

        String startupUrl = getUrlFromIntent(getIntent());
        if (TextUtils.isEmpty(startupUrl)) {
            Log.e(TAG, "Sorry!url is NULL!quit!");
            finish();
            return;
        }

        Log.e(TAG, "Ready to load:" + startupUrl);

        mWebView.loadUrl(startupUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");

        if (mWebView != null) {
            mWebView.resumeTimers();
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e(TAG, "onPause");

        if (mWebView != null) {
            mWebView.pauseTimers();
            mWebView.onPause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        Log.e(TAG, "onDestroy!");

        if (mFlintReceiver != null) {
            unregisterReceiver(mFlintReceiver);
        }

        if (mWebView != null) {
            mWebView.destroy();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO
    }

    private static String getUrlFromIntent(Intent intent) {
        return intent != null ? intent.getDataString() : null;
    }
}
