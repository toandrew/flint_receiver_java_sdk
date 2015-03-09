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

package com.infthink.flintreceiver.sender;

import java.util.concurrent.TimeUnit;

import tv.matchstick.flint.Flint;
import tv.matchstick.flint.MediaStatus;
import tv.matchstick.flint.RemoteMediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.MediaRouteButton;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * This used to demo how to control remote media and send/receive custom message
 */
public class VideoPlayerActivity extends FragmentActivity implements
        FlintStatusChangeListener {
    private static final int AFTER_SEEK_DO_NOTHING = 0;
    private static final int AFTER_SEEK_PLAY = 1;
    private static final int AFTER_SEEK_PAUSE = 2;

    private static final int PLAYER_STATE_NONE = 0;
    private static final int PLAYER_STATE_PLAYING = 1;
    private static final int PLAYER_STATE_PAUSED = 2;
    private static final int PLAYER_STATE_BUFFERING = 3;

    private static final int REFRESH_INTERVAL_MS = (int) TimeUnit.SECONDS
            .toMillis(1);

    private TextView mMediaTitle;
    private TextView mMediaArtist;
    private TextView mAppStatusTextView;
    private TextView mCurrentDeviceTextView;
    private TextView mStreamPositionTextView;
    private TextView mStreamDurationTextView;

    private Button mSendCustMessageButton;
    private TextView mSendCustMsgTextView;
    private TextView mReceivedCustMsgTextView;
    
    private Button mStartMediaButton;
    private Button mPlayPauseButton;
    private Button mStopMediaButton;

    private SeekBar mSeekBar;
    private Spinner mSeekBehaviorSpinner;
    private SeekBar mDeviceVolumeBar;
    private CheckBox mDeviceMuteCheckBox;
    private SeekBar mStreamVolumeBar;
    private CheckBox mStreamMuteCheckBox;

    private CheckBox mAutoplayCheckbox;

    private boolean mSeeking;
    private boolean mIsUserSeeking;
    private boolean mIsUserAdjustingVolume;
    private boolean mIsUserAdjustingMuted;

    private int mPlayerState;

    protected Handler mHandler;
    private Runnable mRefreshRunnable;

    private FlintVideoManager mFlintVideoManager;

    MediaRouteButton mMediaRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fling_player_activity);

        mMediaTitle = (TextView) findViewById(R.id.media_title);
        mMediaArtist = (TextView) findViewById(R.id.media_artist);
        mAppStatusTextView = (TextView) findViewById(R.id.app_status);
        mCurrentDeviceTextView = (TextView) findViewById(R.id.connected_device);
        mStreamPositionTextView = (TextView) findViewById(R.id.stream_position);
        mStreamDurationTextView = (TextView) findViewById(R.id.stream_duration);

        mSendCustMessageButton = (Button) findViewById(R.id.send_cust_msg);
        mSendCustMsgTextView = (TextView) findViewById(R.id.cust_msg);
        mReceivedCustMsgTextView = (TextView) findViewById(R.id.received_msg);
        
        mStartMediaButton = (Button) findViewById(R.id.select_media_button);
        mPlayPauseButton = (Button) findViewById(R.id.pause_play);
        mStopMediaButton = (Button) findViewById(R.id.stop);

        mAutoplayCheckbox = (CheckBox) findViewById(R.id.autoplay_checkbox);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBehaviorSpinner = (Spinner) findViewById(R.id.seek_behavior_spinner);

        mDeviceVolumeBar = (SeekBar) findViewById(R.id.device_volume_bar);
        mDeviceMuteCheckBox = (CheckBox) findViewById(R.id.device_mute_checkbox);
        mStreamVolumeBar = (SeekBar) findViewById(R.id.stream_volume_bar);
        mStreamMuteCheckBox = (CheckBox) findViewById(R.id.stream_mute_checkbox);

        mHandler = new Handler();

        mMediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);

        String applicationId = "~flintplayer";
        Flint.FlintApi.setApplicationId(applicationId);
        mFlintVideoManager = new FlintVideoManager(this, applicationId, this,
                mMediaRouteButton);

        setUpControls();

        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mSeeking) {
                    refreshPlaybackPosition(
                            mFlintVideoManager.getMediaCurrentTime(),
                            mFlintVideoManager.getMediaDuration());
                }
                updateStreamVolume();
                updateButtonStates();
                startRefreshTimer();
            }
        };
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mFlintVideoManager.onStart();
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
        mFlintVideoManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDeviceSelected(String name) {
        setCurrentDeviceName(name);
    }

    @Override
    public void onDeviceUnselected() {
        setCurrentDeviceName(getString(R.string.no_device));
    }

    @Override
    public void onVolumeChanged(double percent, boolean muted) {
        refreshDeviceVolume(percent, muted);
    }

    @Override
    public void onApplicationStatusChanged(String status) {
        setApplicationStatus(status);
    }

    @Override
    public void onApplicationDisconnected() {
        clearMediaState();
        updateButtonStates();
    }

    @Override
    public void onConnectionFailed() {
        updateButtonStates();
        clearMediaState();
        cancelRefreshTimer();
    }

    @Override
    public void onConnected() {
        setDeviceVolumeControlsEnabled(true);
        //mSendCustMessageButton.setEnabled(true);
    }

    @Override
    public void onNoLongerRunning(boolean isRunning) {
        if (isRunning) {
            startRefreshTimer();
        } else {
            clearMediaState();
            updateButtonStates();
        }
    }

    @Override
    public void onConnectionSuspended() {
        cancelRefreshTimer();
        updateButtonStates();
    }

    @Override
    public void onMediaStatusUpdated() {
        MediaStatus mediaStatus = this.mFlintVideoManager.getMediaStatus();
        if ((mediaStatus != null)
                && (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE)) {
            clearMediaState();
        }

        refreshPlaybackPosition(mFlintVideoManager.getMediaCurrentTime(),
                mFlintVideoManager.getMediaDuration());
        updateStreamVolume();
        updateButtonStates();
    }

    @Override
    public void onMediaMetadataUpdated(String title, String artist, Uri imageUrl) {
        setCurrentMediaMetadata(title, artist, imageUrl);
    }

    @Override
    public void onApplicationConnectionResult(String applicationStatus) {
        setApplicationStatus(applicationStatus);
        startRefreshTimer();
        updateButtonStates();
    }

    @Override
    public void onLeaveApplication() {
        updateButtonStates();
    }

    @Override
    public void onStopApplication() {
        updateButtonStates();
    }

    @Override
    public void onMediaSeekEnd() {
        mSeeking = false;
    }

    @Override
    public void onMediaVolumeEnd() {
        mIsUserAdjustingMuted = false;
    }
    
    /**
     * show received custom message.
     * 
     * @param msg
     */
    public void onReceiveCustMessage(String msg) {
        mReceivedCustMsgTextView.setText(msg);;
    }

    /**
     * Setup kinds of controls.
     */
    private void setUpControls() {
        mSendCustMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlintVideoManager.sendCustMsg(mSendCustMsgTextView.getText().toString());
            }
        });
    
        mStartMediaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mFlintVideoManager.loadMedia(mAutoplayCheckbox.isChecked());
            }
        });

        mPlayPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerState == PLAYER_STATE_PAUSED) {
                    mFlintVideoManager.playMedia();
                } else {
                    mFlintVideoManager.pauseMedia();
                }
            }
        });

        mStopMediaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlintVideoManager.stopMedia();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserSeeking = false;
                mSeekBar.setSecondaryProgress(0);
                onSeekBarMoved(TimeUnit.SECONDS.toMillis(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsUserSeeking = true;
                mSeekBar.setSecondaryProgress(seekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
            }
        });

        setUpVolumeControls(mDeviceVolumeBar, mDeviceMuteCheckBox);
        setUpVolumeControls(mStreamVolumeBar, mStreamMuteCheckBox);

        mIsUserSeeking = false;
        mIsUserAdjustingVolume = false;
    }

    /**
     * when seek bar moved
     * 
     * @param position
     */
    private void onSeekBarMoved(long position) {
        if (!mFlintVideoManager.isMediaConnected())
            return;

        refreshPlaybackPosition(position, -1);

        int behavior = mSeekBehaviorSpinner.getSelectedItemPosition();

        int resumeState;
        switch (behavior) {
        case AFTER_SEEK_PLAY:
            resumeState = RemoteMediaPlayer.RESUME_STATE_PLAY;
            break;
        case AFTER_SEEK_PAUSE:
            resumeState = RemoteMediaPlayer.RESUME_STATE_PAUSE;
            break;
        case AFTER_SEEK_DO_NOTHING:
        default:
            resumeState = RemoteMediaPlayer.RESUME_STATE_UNCHANGED;
        }
        mSeeking = true;
        mFlintVideoManager.seekMedia(position, resumeState);
    }

    /**
     * set device name
     * 
     * @param name
     */
    private void setCurrentDeviceName(String name) {
        mCurrentDeviceTextView.setText(name);
    }

    /**
     * refresh current device volume
     * 
     * @param percent
     * @param muted
     */
    private void refreshDeviceVolume(double percent, boolean muted) {
        if (!mIsUserAdjustingVolume) {
            mDeviceVolumeBar
                    .setProgress((int) (percent * FlintVideoManager.MAX_VOLUME_LEVEL));
        }
        mDeviceMuteCheckBox.setChecked(muted);
    }

    /**
     * Set application status
     * 
     * @param statusText
     */
    private void setApplicationStatus(String statusText) {
        mAppStatusTextView.setText(statusText);
    }

    /**
     * Set current media metadata
     * 
     * @param title
     * @param subtitle
     * @param imageUrl
     */
    private void setCurrentMediaMetadata(String title, String subtitle,
            Uri imageUrl) {
        mMediaTitle.setText(title);
        mMediaArtist.setText(subtitle);
    }

    /**
     * Setup volume controls.
     * 
     * @param volumeBar
     * @param muteCheckBox
     */
    private void setUpVolumeControls(final SeekBar volumeBar,
            final CheckBox muteCheckBox) {
        volumeBar.setMax((int) FlintVideoManager.MAX_VOLUME_LEVEL);
        volumeBar.setProgress(0);
        volumeBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mIsUserAdjustingVolume = false;
                        volumeBar.setSecondaryProgress(0);
                        if (volumeBar == mDeviceVolumeBar) {
                            mFlintVideoManager.setDeviceVolume(seekBar
                                    .getProgress());
                        } else {
                            mFlintVideoManager.setMediaVolume(seekBar
                                    .getProgress());
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mIsUserAdjustingVolume = true;
                        volumeBar.setSecondaryProgress(seekBar.getProgress());
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progress, boolean fromUser) {
                    }
                });

        muteCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view,
                            boolean isChecked) {
                        if (muteCheckBox == mDeviceMuteCheckBox) {
                            mFlintVideoManager.setDeviceMute(isChecked);
                        } else {
                            mIsUserAdjustingMuted = true;
                            mFlintVideoManager.setMediaMute(isChecked);
                        }
                    }
                });
    }

    /**
     * Refresh current playback position
     * 
     * @param position
     * @param duration
     */
    private void refreshPlaybackPosition(long position, long duration) {
        if (!mIsUserSeeking) {
            if (position == 0) {
                mStreamPositionTextView.setText(R.string.no_time);
                mSeekBar.setProgress(0);
            } else if (position > 0) {
                mSeekBar.setProgress((int) TimeUnit.MILLISECONDS
                        .toSeconds(position));
            }
            mStreamPositionTextView.setText(formatTime(position));
        }

        if (duration == 0) {
            mStreamDurationTextView.setText(R.string.no_time);
            mSeekBar.setMax(0);
        } else if (duration > 0) {
            mStreamDurationTextView.setText(formatTime(duration));
            if (!mIsUserSeeking) {
                mSeekBar.setMax((int) TimeUnit.MILLISECONDS.toSeconds(duration));
            }
        }
    }

    /**
     * Refresh stream volume
     * 
     * @param percent
     * @param muted
     */
    private void refreshStreamVolume(double percent, boolean muted) {
        if (!mIsUserAdjustingVolume) {
            mStreamVolumeBar
                    .setProgress((int) (percent * FlintVideoManager.MAX_VOLUME_LEVEL));
        }
        if (!mIsUserAdjustingMuted) {
            mStreamMuteCheckBox.setChecked(muted);
        }
    }

    /**
     * Update stream volume
     */
    private void updateStreamVolume() {
        MediaStatus mediaStatus = mFlintVideoManager.getMediaStatus();
        if (mediaStatus != null) {
            double streamVolume = mediaStatus.getStreamVolume();
            boolean muteState = mediaStatus.isMute();
            refreshStreamVolume(streamVolume, muteState);
        }
    }

    private String formatTime(long millisec) {
        int seconds = (int) (millisec / 1000);
        int hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        int minutes = seconds / 60;
        seconds %= 60;

        String time;
        if (hours > 0) {
            time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            time = String.format("%d:%02d", minutes, seconds);
        }
        return time;
    }

    /**
     * Update the buttons' state according to current status.
     */
    private void updateButtonStates() {
        boolean hasDeviceConnection = mFlintVideoManager.isDeviceConnected();
        boolean hasAppConnection = mFlintVideoManager.isAppConnected();
        boolean hasMediaConnection = mFlintVideoManager.isMediaConnected();
        boolean hasMedia = false;

        if (hasMediaConnection) {
            MediaStatus mediaStatus = mFlintVideoManager.getMediaStatus();
            if (mediaStatus != null) {
                int mediaPlayerState = mediaStatus.getPlayerState();
                int playerState = PLAYER_STATE_NONE;
                if (mediaPlayerState == MediaStatus.PLAYER_STATE_PAUSED) {
                    playerState = PLAYER_STATE_PAUSED;
                } else if (mediaPlayerState == MediaStatus.PLAYER_STATE_PLAYING) {
                    playerState = PLAYER_STATE_PLAYING;
                } else if (mediaPlayerState == MediaStatus.PLAYER_STATE_BUFFERING) {
                    playerState = PLAYER_STATE_BUFFERING;
                }
                setPlayerState(playerState);

                hasMedia = mediaStatus.getPlayerState() != MediaStatus.PLAYER_STATE_IDLE;
            }
        } else {
            setPlayerState(PLAYER_STATE_NONE);
        }

        mSendCustMessageButton.setEnabled(hasMediaConnection);

        mAutoplayCheckbox.setEnabled(hasDeviceConnection && hasAppConnection);

        mStartMediaButton.setEnabled(hasMediaConnection);
        mStopMediaButton.setEnabled(hasMediaConnection && hasMedia);
        setSeekBarEnabled(hasMediaConnection && hasMedia);
        setDeviceVolumeControlsEnabled(hasDeviceConnection);
        setStreamVolumeControlsEnabled(hasMediaConnection && hasMedia);
    }

    /**
     * set seekbar's status
     * 
     * @param enabled
     */
    private void setSeekBarEnabled(boolean enabled) {
        mSeekBar.setEnabled(enabled);
    }

    /**
     * set whether device volume controls is enabled.
     * 
     * @param enabled
     */
    private void setDeviceVolumeControlsEnabled(boolean enabled) {
        mDeviceVolumeBar.setEnabled(enabled);
        mDeviceMuteCheckBox.setEnabled(enabled);
    }
    
    /**
     * set whether stream volume controls is enabled.
     * 
     * @param enabled
     */
    private void setStreamVolumeControlsEnabled(boolean enabled) {
        mStreamVolumeBar.setEnabled(enabled);
        mStreamMuteCheckBox.setEnabled(enabled);
    }

    /**
     * set current player's state
     * 
     * @param playerState
     */
    private void setPlayerState(int playerState) {
        mPlayerState = playerState;
        if (mPlayerState == PLAYER_STATE_PAUSED) {
            mPlayPauseButton.setText(R.string.play);
        } else if (mPlayerState == PLAYER_STATE_PLAYING) {
            mPlayPauseButton.setText(R.string.pause);
        }

        mPlayPauseButton.setEnabled((mPlayerState == PLAYER_STATE_PAUSED)
                || (mPlayerState == PLAYER_STATE_PLAYING));
    }

    /**
     * clear media state
     */
    private void clearMediaState() {
        setCurrentMediaMetadata(null, null, null);
        refreshPlaybackPosition(0, 0);
    }

    /**
     * start refresh task
     */
    protected final void startRefreshTimer() {
        mHandler.postDelayed(mRefreshRunnable, REFRESH_INTERVAL_MS);
    }

    /**
     * cancel refresh task.
     */
    protected final void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }
}
