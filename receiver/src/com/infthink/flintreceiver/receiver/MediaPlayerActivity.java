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

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.widget.MediaController.OnHiddenListener;
import io.vov.vitamio.widget.MediaController.OnShownListener;
import io.vov.vitamio.widget.VideoView;

import org.json.JSONObject;

import tv.matchstick.flintreceiver.FlintReceiverManager;
import tv.matchstick.flintreceiver.R;
import tv.matchstick.flintreceiver.ReceiverMessageBus;
import tv.matchstick.flintreceiver.media.FlintMediaPlayer;
import tv.matchstick.flintreceiver.media.FlintVideo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.infthink.flintreceiver.receiver.widget.MyMediaController;
import com.infthink.flintreceiver.receiver.widget.MyMediaController.OnChangeMediaStateListener;

/**
 * This is test application which will use Flint Java Receiver SDK ad Vitamio
 * SDK.
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
public class MediaPlayerActivity extends Activity {

    private static final String TAG = MediaPlayerActivity.class.getSimpleName();

    private static final String APPID = "~flintplayer";

    private static final String CUST_MESSAGE_NAMESPACE = "urn:flint:com.infthink.flintreceiver.receiver";

    // custom message which will be send back to Sender Apps.
    private JSONObject mCustMessage;

    private ReceiverMessageBus mCustMessageReceiverMessageBus = null;

    private VideoView mVideoView;

    private MyMediaController mFlintMediaController;

    private LinearLayout mLinearLayout;

    // logo image
    private ImageView mLogoView;

    private FrameLayout mFrameLayout;

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
                if (mVideoView != null) {
                    return mVideoView.getCurrentPosition();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return mCurrentTime;
        }

        @Override
        public void setVolume(double volume) {
            mVolume = volume; // save this volume.

            if (mVolume == 0) {
                mMuted = true;
            } else {
                mMuted = false;
            }

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

        IntentFilter filter = new IntentFilter("fling.action.stop_receiver");
        registerReceiver(mFlintReceiver, filter);

        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }

        mVideoView = new VideoView(this);
        
        // Hardware Decoder? default
        mVideoView.setHardwareDecoder(true);

        mVideoView
                .setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        // TODO Auto-generated method stub

                        Log.e(TAG, "onBufferingUpdate:percent[" + percent + "]");
                    }
                });

        mVideoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub

                Log.e(TAG, "OnErrorListener:what[" + what + "]extra[" + extra
                        + "]");

                mFlintVideo.notifyEvents(FlintVideo.ERROR, "Media ERROR");

                return false;
            }

        });

        mVideoView.setOnInfoListener(new OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub

                Log.e(TAG, "OnInfoListener:what[" + what + "]extra[" + extra
                        + "]");

                mFlintVideo.setCurrentTime(mp.getCurrentPosition());

                switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.e(TAG, "waiting?!");
                    mFlintVideo.notifyEvents(FlintVideo.WAITING,
                            "Media is WAITING");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (mVideoView.isPlaying()) {
                        Log.e(TAG, "MEDIA_INFO_BUFFERING_END: playing?!");
                        mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                                "Media is PLAYING");
                    } else {
                        Log.e(TAG, "MEDIA_INFO_BUFFERING_END: waiting!!!?!");
                        
                        // this should be a workaround for the seek issue of VideoView in PAUSE state.
                        mFlintVideo.notifyEvents(FlintVideo.SEEKED,
                                "Media is WAITING?");
                        mFlintVideo.notifyEvents(FlintVideo.PAUSE,
                                "Media is PAUSED?");
                    }
                    break;
                }

                return false;
            }

        });

        mVideoView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub

                mFrameLayout.setVisibility(View.GONE);

                // mTextView.setVisibility(View.GONE);

                Log.e(TAG, "onPrepared![" + mp.getDuration() + "]");

                // set Flint related
                mFlintVideo.setDuration(mp.getDuration());

                mFlintVideo.setPlaybackRate(1); // TODO

                mFlintVideo.setCurrentTime(mp.getCurrentPosition());

                mFlintVideo.notifyEvents(FlintVideo.LOADEDMETADATA,
                        "Media is LOADEDMETADATA"); // READY TO PLAY
                // TO
                // PLAY
            }

        });

        mVideoView.setOnSeekCompleteListener(new OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                // TODO Auto-generated method stub

                Log.e(TAG, "onSeekComplete!");

                // notify sender app this SEEKED event.
                mFlintVideo.notifyEvents(FlintVideo.SEEKED, "Media SEEKED");
            }

        });

        mVideoView.setOnCompletionListener(new OnCompletionListener() {

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

        mFlintMediaController = new MyMediaController(this);
        mFlintMediaController
                .setOnChangeMediaStateListener(new OnChangeMediaStateListener() {

                    @Override
                    public void seekEnd(long position) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void startMedia() {
                        // TODO Auto-generated method stub

                        // called when user clicked the "play" button in DONGLE
                        // side.
                        // in order to let sender app's know current status.
                        mFlintVideo.notifyEvents(FlintVideo.PLAYING,
                                "Media is PLAYING");
                    }

                    @Override
                    public void pauseMedia() {
                        // TODO Auto-generated method stub

                        // called when user clicked the "pause" button in DONGLE
                        // side.
                        // in order to let sender app's know current status.
                        mFlintVideo.notifyEvents(FlintVideo.PAUSE,
                                "PAUSE Media");
                    }

                    @Override
                    public long getCurrentPosition() {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public long getDuration() {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                });

        mFlintMediaController.setOnHiddenListener(new OnHiddenListener() {
            @Override
            public void onHidden() {
            }
        });

        mFlintMediaController.setOnShownListener(new OnShownListener() {
            @Override
            public void onShown() {
            }
        });

        mVideoView.setMediaController(mFlintMediaController);
        mVideoView.requestFocus();

        // init content view
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.setBackgroundColor(Color.BLACK);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);

        mLogoView = new ImageView(this);
        mLogoView.setScaleType(ScaleType.CENTER_INSIDE);
        mLogoView.setImageResource(R.drawable.logo);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        mLinearLayout.addView(mLogoView, lp);

        mFrameLayout.addView(mLinearLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        mFrameLayout.setVisibility(View.VISIBLE);

        final FrameLayout contentView = new FrameLayout(this);

        contentView.addView(mVideoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        contentView.addView(mFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        contentView.setBackgroundColor(Color.BLACK);

        setContentView(contentView);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = (float) am.getStreamVolume(AudioManager.STREAM_MUSIC)
                / (float) maxVolume;

        if (mVolume == 0) {
            mMuted = true;
        } else {
            mMuted = false;
        }

        // init flint related objects.
        init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
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
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy!");
        try {
            if (mFlintReceiver != null) {
                unregisterReceiver(mFlintReceiver);
            }

            mFlintMediaPlayer.stop(null);

            mFlintReceiverManager.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mVideoView != null) {
                mVideoView.stopPlayback();
                mVideoView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            public boolean onMediaMessage(String payload) {
                // TODO, here you can process all media messages.

                Log.e(TAG, "onMediaMessages: " + payload);

                return false;
            }
        };

        // used to receive cust message from sender app.
        mCustMessageReceiverMessageBus = new ReceiverMessageBus(
                CUST_MESSAGE_NAMESPACE) {

            @Override
            public void onPayloadMessage(final String payload,
                    final String senderId) {
                // TODO Auto-generated method stub

                // process CUSTOM messages received from sender apps.
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(),
                                "Got user messages![" + payload + "]",
                                Toast.LENGTH_SHORT).show();
                    }

                });

            }
        };

        mFlintReceiverManager.setMessageBus(CUST_MESSAGE_NAMESPACE,
                mCustMessageReceiverMessageBus);

        mFlintReceiverManager.open();
    }

    /**
     * Process LOAD media player event
     */
    private void doLoad() {

        try {
            if (mVideoView != null) {
                mVideoView.setVideoPath(mFlintVideo.getUrl());
                mVideoView.start();
                Log.e(TAG, "setVideoPath![" + mFlintVideo.getUrl() + "]");
            } else {
                Log.e(TAG, "mMediaPlayer is null?!");
            }

            // hide media controller?
            mFlintMediaController.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process PLAY media player event.
     */
    private void doPlay() {
        try {
            if (mVideoView != null) {
                mVideoView.start();
            }

            mFlintVideo.notifyEvents(FlintVideo.PLAY, "PLAY Media");

            // hide media controller?
            mFlintMediaController.hide();

            // Here show how to send custom message to sender apps.
            mCustMessage = new JSONObject();
            mCustMessage.put("hello", "PLAY Media!");
            mHandler.sendEmptyMessage(PLAYER_MSG_SEND_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process PAUSE media player event.
     */
    private void doPause() {
        try {
            if (mVideoView != null) {
                mVideoView.pause();
            }

            mFlintVideo.notifyEvents(FlintVideo.PAUSE, "PAUSE Media");

            // show media controller
            mFlintMediaController.show();
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
            if (mVideoView != null) {
                mVideoView.seekTo(msec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // notify seeking event to sender apps!!!
        mFlintVideo.notifyEvents(FlintVideo.WAITING, "Media VOLUMECHANGED");
    }

    /**
     * Process CHANGE VOLUME media player event.
     */
    private void doChangeVolume() {
        try {
            if (mVideoView != null) {
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
        // Here show how to send custom message to sender apps.
        // mCustMessage = new JSONObject();
        // mCustMessage.put("hello", "PLAY Media!");
        // mHandler.sendEmptyMessage(PLAYER_MSG_SEND_MESSAGE);

        if (mCustMessageReceiverMessageBus != null && mCustMessage != null) {
            Log.e(TAG, "doSendMessage!" + mCustMessage);

            mCustMessageReceiverMessageBus.send(mCustMessage.toString(), null); // null:
                                                                                // send
                                                                                // to
                                                                                // all.
        }
    }

    /**
     * Process STOP media player event.
     */
    private void doStop() {
        try {
            if (mVideoView != null) {
                mVideoView.stopPlayback();
            }

            // notify that media player is stopped!
            mFlintVideo.notifyEvents(FlintVideo.ENDED, "Media ENDED");

            // STOP received? treated it as FINISHED!
            mHandler.sendEmptyMessage(PLAYER_MSG_FINISHED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Do something when video is finished!
     */
    private void doFinished() {
        mFrameLayout.setVisibility(View.VISIBLE);

        Toast.makeText(getApplicationContext(), "The video is finished!",
                Toast.LENGTH_SHORT).show();
    }
}
