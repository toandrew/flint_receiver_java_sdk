/*
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

import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

/**
 * Use to contain all receivers.
 */
public class FlintContainerActivity extends Activity {
    private static final String TAG = "FlintContainerActivity";

    private XWalkView mXWalkView;

    BroadcastReceiver mFlintReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.e(TAG, "Ready to call finish!!!");
            finish();
            Log.e(TAG, "End to call finish!!!");
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate!");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.flint_container);

        mXWalkView = (XWalkView) findViewById(R.id.container_main);

        IntentFilter filter = new IntentFilter("fling.action.stop_receiver");
        registerReceiver(mFlintReceiver, filter);

        String startupUrl = getUrlFromIntent(getIntent());
        if (TextUtils.isEmpty(startupUrl)) {
            Log.e(TAG, "Sorry!url is NULL!quit!");
            finish();
            return;
        }

        Log.e(TAG, "Ready to load:" + startupUrl);

        mXWalkView.load(startupUrl, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");

        if (mXWalkView != null) {
            mXWalkView.resumeTimers();
            mXWalkView.onShow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e(TAG, "onPause");

        if (mXWalkView != null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
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
        super.onDestroy();

        Log.e(TAG, "onDestroy!");

        if (mFlintReceiver != null) {
            unregisterReceiver(mFlintReceiver);
        }

        if (mXWalkView != null) {
            mXWalkView.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mXWalkView != null) {
            mXWalkView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mXWalkView != null) {
            mXWalkView.onNewIntent(intent);
        }
    }

    private static String getUrlFromIntent(Intent intent) {
        return intent != null ? intent.getDataString() : null;
    }
}
