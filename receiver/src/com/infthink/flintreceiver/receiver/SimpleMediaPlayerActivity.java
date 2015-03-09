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

import org.json.JSONObject;

import tv.matchstick.flintreceiver.FlintReceiverManager;
import tv.matchstick.flintreceiver.R;
import tv.matchstick.flintreceiver.media.FlintMediaPlayer;
import tv.matchstick.flintreceiver.media.FlintVideo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * This is test application which will use Flint Java Receiver SDK.
 *
 * How to use the Java Receiver SDK?
 *
 * 1. Make sure "FlintReceiverManager" object is created.
 * <p>
 * mFlintReceiverManager = new FlintReceiverManager(APPID); //
 * APPID("~flintplayer") IS OUR DEFAULT MEDIA PLAYER APPLICATION'S ID.
 * <p>
 *
 * 2. Create concrete "FlingVideo" object and implements all abstract functions.
 * <p>
 * mFlintVideo = new MyFlintVideo(); // MyFlintVideo is extended from the
 * abstract class FlintVideo.
 * <p>
 *
 * 3. Create FlintMediaPlayer object which will handle all Flint media player
 * related messages.
 * <p>
 * mFlintMediaPlayer = new FlintMediaPlayer(mFlintReceiverManager, mFlintVideo);
 * <p>
 *
 * 4. Call "open" of FlintReceiverManager to receive and process all Flint media
 * related messages.
 * <p>
 * mFlintReceiverManager.open(); // or mFlintReceiverManager.close() to release
 * it.
 * <p>
 *
 * @author jim
 *
 */
public class SimpleMediaPlayerActivity extends Activity implements Callback {
    private static final String TAG = "SimpleMediaPlayerActivity";

    private static final String APPID = "~flintplayer";

    private static final int PLAYER_MSG_LOAD = 1;
    private static final int PLAYER_MSG_PLAY = 2;
    private static final int PLAYER_MSG_PAUSE = 3;
    private static final int PLAYER_MSG_SEEK = 4;
    private static final int PLAYER_MSG_CHANGE_VOLUME = 5;
    private static final int PLAYER_MSG_SEND_MESSAGE = 6;
    private static final int PLAYER_MSG_STOP = 7;
    private static final int PLAYER_MSG_FINISHED = 8;

    private FlintReceiverManager mFlintReceiverManager;

    private MyFlintVideo mFlintVideo;

    private FlintMediaPlayer mFlintMediaPlayer;

    private Button mPlayBtn;
    private Button mPauseBtn;
    private Button mStopBtn;

    private MediaPlayer mMediaPlayer = null;
    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;

    private double mCurrentTime = 0;

    private boolean mMuted = false;

    private double mVolume = 0; // please note the category: "0.0" ~ "1.0"

    /**
     * Use the followings to process all standard media events: LOAD, PLAY,
     * PAUSE,etc
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
            case PLAYER_MSG_LOAD:
                doLoad();
                break;

            case PLAYER_MSG_PLAY:
                doPlay();
                break;

            case PLAYER_MSG_PAUSE:
                doPause();
                break;

            case PLAYER_MSG_SEEK:
                doSeek(msg.arg1);
                break;

            case PLAYER_MSG_CHANGE_VOLUME:
                doChangeVolume();
                break;

            case PLAYER_MSG_SEND_MESSAGE:
                doSendMessage();
                break;

            case PLAYER_MSG_STOP:
                doStop();
                break;

            case PLAYER_MSG_FINISHED:
                doFinished();
                break;
            }
        }
    };

    /**
     * Concrete Flint Video, which will receive all media events.
     */
    private class MyFlintVideo extends FlintVideo {

        @Override
        public void load() {
            Log.e(TAG, "load!");

            mHandler.sendEmptyMessage(PLAYER_MSG_LOAD);
        }

        @Override
        public void pause() {
            Log.e(TAG, "pause!");

            mHandler.sendEmptyMessage(PLAYER_MSG_PAUSE);
        }

        @Override
        public void play() {
            Log.e(TAG, "play!");

            mHandler.sendEmptyMessage(PLAYER_MSG_PLAY);
        }

        @Override
        public void stop(JSONObject custData) {
            Log.e(TAG, "stop!");

            mHandler.sendEmptyMessage(PLAYER_MSG_STOP);
        }

        @Override
        public void seek(double time) {
            mCurrentTime = time;

            Message msg = mHandler.obtainMessage();
            msg.what = PLAYER_MSG_SEEK;
            msg.arg1 = (int) time;
            mHandler.sendMessage(msg);
        }

        @Override
        public void setCurrentTime(double time) {
            // TODO Auto-generated method stub

            mCurrentTime = time;
        }

        @Override
        public double getCurrentTime() {
            // use real concrete media object to getCurrent position!

            try {
                if (mMediaPlayer != null) {
                    return mMediaPlayer.getCurrentPosition();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return mCurrentTime;
        }

        @Override
        public void setVolume(double volume) {
            mVolume = volume; // save this volume.

            Message msg = mHandler.obtainMessage();
            msg.what = PLAYER_MSG_CHANGE_VOLUME;
            mHandler.sendMessage(msg);
        }

        @Override
        public double getVolume() {
            // TODO Auto-generated method stub

            return mVolume;
        }

        @Override
        public boolean isMuted() {
            // TODO Auto-generated method stub

            return mMuted;
        }
    };

    /**
     * Process "STOP_RECEIVER" command from Flingd
     */
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

        mPlayBtn = (Button) findViewById(R.id.play);
        mPlayBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                mHandler.sendEmptyMessage(PLAYER_MSG_PLAY);
            }

        });

        mPauseBtn = (Button) findViewById(R.id.pause);
        mPauseBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                mHandler.sendEmptyMessage(PLAYER_MSG_PAUSE);
            }

        });

        mStopBtn = (Button) findViewById(R.id.stop);
        mStopBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                mHandler.sendEmptyMessage(PLAYER_MSG_STOP);
            }
        });

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);

        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(this);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = (float) am.getStreamVolume(AudioManager.STREAM_MUSIC)
                / (float) maxVolume;

        // init flint related objects
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

        try {
            mFlintMediaPlayer.stop(null);

            mFlintReceiverManager.close();

            if (mFlintReceiver != null) {
                unregisterReceiver(mFlintReceiver);
            }

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();

                mMediaPlayer.release();

                mMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer
                .setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        // TODO Auto-generated method stub

                        Log.e(TAG, "onBufferingUpdate:percent[" + percent + "]");
                    }

                });

        mMediaPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub

                Log.e(TAG, "OnErrorListener:what[" + what + "]extra[" + extra
                        + "]");

                mFlintVideo.notifyEvents(FlintVideo.ERROR, "Media ERROR");

                return false;
            }

        });

        mMediaPlayer.setOnInfoListener(new OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub

                Log.e(TAG, "OnInfoListener:what[" + what + "]extra[" + extra
                        + "]");

                mFlintVideo.setCurrentTime(mp.getCurrentPosition());

                switch (what) {
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                            "Media is PLAYING");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mFlintVideo.notifyEvents(FlintVideo.WAITING,
                            "Media is WAITING");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                            "Media is PLAYING");
                    break;
                case MediaPlayer.MEDIA_INFO_UNKNOWN:
                    mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                            "Media is PLAYING");
                    break;
                case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                            "Media is PLAYING");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                            "Media is PLAYING");
                    break;
                case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                            "Media is PLAYING");
                    break;
                }

                return false;
            }

        });

        mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub

                Log.e(TAG, "onPrepared![" + mp.getDuration() + "]");

                // set Flint related
                mFlintVideo.setDuration(mp.getDuration());

                mFlintVideo.setPlaybackRate(1); // TODO

                mFlintVideo.setCurrentTime(mp.getCurrentPosition());

                mFlintVideo.notifyEvents(FlintVideo.LOADEDMETADATA,
                        "Media is LOADEDMETADATA"); // READY
                // TO
                // PLAY
            }

        });

        mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                // TODO Auto-generated method stub

                Log.e(TAG, "onSeekComplete!");

                // notify sender app this SEEKED event.
                mFlintVideo.notifyEvents(FlintVideo.SEEKED, "Media SEEKED");
            }

        });

        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub

                Log.e(TAG, "onCompletion!");

                mFlintVideo.setCurrentTime(mFlintVideo.getDuration());

                // notify sender app this media ended event.
                mFlintVideo.notifyEvents(FlintVideo.ENDED, "Media ENDED");

                mHandler.sendEmptyMessage(PLAYER_MSG_FINISHED);
            }
        });

        mMediaPlayer
                .setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width,
                            int height) {

                        changeVideoSizes(width, height);
                    }
                });

        mMediaPlayer.setDisplay(mSurfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    /**
     * Init all Flint related objects
     */
    private void init() {

        // whether enable receiver log.
        FlintReceiverManager.setLogEnabled(true);

        mFlintReceiverManager = new FlintReceiverManager(APPID);

        mFlintVideo = new MyFlintVideo();

        mFlintMediaPlayer = new FlintMediaPlayer(mFlintReceiverManager,
                mFlintVideo) {

            @Override
            public boolean onMediaMessages(String payload) {
                // TODO, here you can process all media messages.

                Log.e(TAG, "onMediaMessages: " + payload);
                return false;
            }
        };

        mFlintReceiverManager.open();
    }

    /**
     * Process LOAD media player event
     */
    private void doLoad() {

        try {
            if (mMediaPlayer != null) {

                // in order to continue play, first we stop and rest media
                // player!
                try {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mMediaPlayer.setDataSource(mFlintVideo.getUrl());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } else {
                Log.e(TAG, "mMediaPlayer is null?!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process PLAY media player event.
     */
    private void doPlay() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }

            mFlintVideo.notifyEvents(FlintVideo.PLAY, "PLAY Media");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process PAUSE media player event.
     */
    private void doPause() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }

            mFlintVideo.notifyEvents(FlintVideo.PAUSE, "PAUSE Media");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process SEEK media player event.
     */
    private void doSeek(int msec) {
        Log.e(TAG, "seek![" + msec);

        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(msec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process CHANGE VOLUME media player event.
     */
    private void doChangeVolume() {
        try {
            if (mMediaPlayer != null) {
                double volume = mFlintVideo.getVolume(); // 0.0 ~ 1.0
                Log.e(TAG, "doChangeVolume:volume:" + volume);

                // change system volume?
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int maxVolume = am
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                Log.e(TAG, "maxVolume:" + maxVolume);
                am.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int) (maxVolume * volume),
                        AudioManager.FLAG_PLAY_SOUND
                                | AudioManager.FLAG_SHOW_UI);

                // notify volume changed event to sender apps!!!
                mFlintVideo.notifyEvents(FlintVideo.VOLUMECHANGE,
                        "Media VOLUMECHANGED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Send CUSTOM MESSAGE events to Sender Apps.
     */
    private void doSendMessage() {

    }

    /**
     * Process STOP media player event.
     */
    private void doStop() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }

            // notify that media player is stopped!
            mFlintVideo.notifyEvents(FlintVideo.ENDED, "Media ENDED");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Do something when video is finished!
     */
    private void doFinished() {
        Toast.makeText(getApplicationContext(), "The video is finished!",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Change current video surface's width and height.
     *
     * @param width
     * @param height
     */
    private void changeVideoSizes(int width, int height) {
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height
                    + ")");
            return;
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int displayWith = dm.widthPixels;
        int displayHeight = dm.heightPixels;

        if (width != 0 && height != 0) {
            // LayoutParams params = mSurface.getLayoutParams();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    displayWith, displayHeight);

            if (width * displayHeight > displayWith * height) {
                params.height = displayWith * height / width;
            } else if (width * displayHeight < displayWith * height) {
                params.width = displayHeight * width / height;
            } else {
                params.width = displayWith;
                params.height = displayHeight;
            }

            Log.e(TAG, "displayWith: " + displayWith + " displayHeight:"
                    + displayHeight + " params.width:" + params.width
                    + " params.height:" + params.height);
            int marginLeft = (displayWith - params.width) / 2;
            int marginTop = (displayHeight - params.height) / 2;

            Log.e(TAG, "marginLeft:" + marginLeft + " marginTop:" + marginTop);

            params.setMargins(marginLeft, marginTop, marginLeft, marginTop);
            mSurfaceView.setLayoutParams(params);

            mSurfaceHolder.setFixedSize(params.width, params.height);
        }
    }
}
