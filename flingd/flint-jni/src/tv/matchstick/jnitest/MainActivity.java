package tv.matchstick.jnitest;

import tv.matchstick.Flint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements Flint.Callback {
    private static final String LOG_TAG = "Flint";

    public static final String FLINT_DEFAULT_MEDIA_APP_URL = "http://openflint.github.io/flint-player/player.html";

    public static final String ACTION_STOP_RECEIVER = "fling.action.stop_receiver";

    public Button startBt;
    public Button stopBt;
    public Button statusBt;

    private Flint flint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBt = (Button) findViewById(R.id.start);
        stopBt = (Button) findViewById(R.id.stop);
        statusBt = (Button) findViewById(R.id.getstatus);

        flint = new Flint(this);
        flint.setCallback(this);

        startBt.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (flint != null && !flint.isRunning()) {
                    new Thread(new Runnable() {
                        public void run() {
                            flint.start();
                            int e = flint.getErrorCode();
                            Log.d(LOG_TAG, "Flint start error:" + e);
                        }
                    }).start();
                }
            }
        });

        stopBt.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (flint != null && flint.isRunning()) {
                    flint.stop();
                }
            }
        });

        statusBt.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (flint != null) {
                    Toast.makeText(
                            getApplicationContext(),
                            "FlintDaemon is "
                                    + (flint.isRunning() ? "running"
                                            : "not running"),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "FlintDaemon is not running", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        if (flint != null && flint.isRunning()) {
            flint.stop();
        }
        flint = null;
    }

    @Override
    public void onWebAppStart(String appInfo) {
        // TODO Auto-generated method stub
        Log.d(LOG_TAG, "start web app: " + appInfo);

        doLaunchWebApp(appInfo);
    }

    @Override
    public void onNativeAppStart(String appInfo) {
        // TODO Auto-generated method stub
        Log.d(LOG_TAG, "start native app: " + appInfo);

        doLaunchNativeApp(appInfo);
    }

    @Override
    public void onWebAppStop(String appInfo) {
        // TODO Auto-generated method stub
        Log.d(LOG_TAG, "stop web app: " + appInfo);

        // stop web app
        doStop();
    }

    @Override
    public void onNativeAppStop(String appInfo) {
        // TODO Auto-generated method stub
        Log.d(LOG_TAG, "stop native app: " + appInfo);

        // stop native app
        doStop();
    }

    /**
     * start web app
     * 
     * @param url
     */
    private void doLaunchWebApp(String url) {
        Log.e(LOG_TAG, "ur:" + url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri CONTENT_URI_BROWSERS = Uri.parse(url);
        intent.setData(CONTENT_URI_BROWSERS);

        // if it's a default media app, let's use android native media player.
        if (FLINT_DEFAULT_MEDIA_APP_URL.equals(url)) {
            intent.setClassName("com.infthink.flintreceiver.receiver",
                    "com.infthink.flintreceiver.receiver.SimpleMediaPlayerActivity");
        } else {
            // use Web(crosswalk) to open Flint receiver apps.
            intent.setClassName("com.infthink.flintreceiver.receiver",
                    "com.infthink.flintreceiver.receiver.FlintContainerActivity");
        }

        MainActivity.this.startActivity(intent);
    }

    /**
     * start native app
     * 
     * @param url
     */
    void doLaunchNativeApp(String url) {
        // TODO
    }

    /**
     * Stop receiver application.
     */
    void doStop() {
        Log.e(LOG_TAG, "Stop receiver!");

        // ready to stop receiver application?
        Intent intent = new Intent();
        intent.setAction(ACTION_STOP_RECEIVER);
        MainActivity.this.sendBroadcast(intent);
    }
}
