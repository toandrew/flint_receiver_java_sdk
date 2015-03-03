package tv.matchstick.flintreceiver;

import tv.matchstick.flintreceiver.media.FlintMediaPlayer;
import tv.matchstick.flintreceiver.media.FlintVideo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final String APPID = "~flintplayer";

    private FlintReceiverManager mFlintReceiverManager;

    private FlintVideo mFlintVideo;

    private FlintMediaPlayer mFlintMediaPlayer;

    BroadcastReceiver mFlintReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.e(TAG, "Ready to call finish!!!");
            finish();
            Log.e(TAG, "End to call finish!!!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter("fling.action.stop_receiver");
        registerReceiver(mFlintReceiver, filter);

        init();
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mFlintMediaPlayer.stop(null);

        mFlintReceiverManager.close();

        if (mFlintReceiver != null) {
            unregisterReceiver(mFlintReceiver);
        }
    }

    /**
     * init objects
     */
    private void init() {
        mFlintReceiverManager = new FlintReceiverManager(APPID);
        mFlintVideo = new FlintVideo();
        mFlintMediaPlayer = new FlintMediaPlayer(mFlintReceiverManager,
                mFlintVideo);

        mFlintReceiverManager.open();
    }
}
