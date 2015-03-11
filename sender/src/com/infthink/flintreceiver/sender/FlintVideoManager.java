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

import java.io.IOException;
import java.util.List;

import tv.matchstick.flint.ApplicationMetadata;
import tv.matchstick.flint.ConnectionResult;
import tv.matchstick.flint.Flint;
import tv.matchstick.flint.Flint.ApplicationConnectionResult;
import tv.matchstick.flint.FlintDevice;
import tv.matchstick.flint.FlintManager;
import tv.matchstick.flint.FlintMediaControlIntent;
import tv.matchstick.flint.MediaInfo;
import tv.matchstick.flint.MediaMetadata;
import tv.matchstick.flint.MediaStatus;
import tv.matchstick.flint.RemoteMediaPlayer;
import tv.matchstick.flint.RemoteMediaPlayer.MediaChannelResult;
import tv.matchstick.flint.ResultCallback;
import tv.matchstick.flint.Status;
import tv.matchstick.flint.images.WebImage;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;

public class FlintVideoManager {
    private static final String TAG = FlintVideoManager.class.getSimpleName();

    public static final double VOLUME_INCREMENT = 0.05;
    public static final double MAX_VOLUME_LEVEL = 20;

    private Context mContext;
    private Handler mHandler;
    private String mApplicationId;

    private FlintStatusChangeListener mStatusChangeListener;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private FlintMediaRouterCallback mMediaRouterCallback;
    private FlintDevice mSelectedDevice;
    private FlintManager mApiClient;
    private FlintListener mFlintListener;
    private ConnectionCallbacks mConnectionCallbacks;

    private MediaInfo mMediaInfo;
    private RemoteMediaPlayer mMediaPlayer;
    private ApplicationMetadata mAppMetadata;

    private boolean mWaitingForReconnect;

    MediaRouteButton mMediaRouteButton;

    FlintMsgChannel mFlintMsgChannel;

    public FlintVideoManager(Context context, String applicationId,
            FlintStatusChangeListener listener,
            MediaRouteButton mediaRouteButton) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mApplicationId = applicationId;
        mStatusChangeListener = listener;
        mMediaRouteButton = mediaRouteButton;

        Log.d(TAG, "Application ID is: " + mApplicationId);
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        FlintMediaControlIntent
                                .categoryForFlint(mApplicationId)).build();

        mMediaRouteButton.setRouteSelector(mMediaRouteSelector);

        mMediaRouterCallback = new FlintMediaRouterCallback();

        mConnectionCallbacks = new ConnectionCallbacks();

        mFlintListener = new FlintListener();

        // send/receive custom message.
        mFlintMsgChannel = new FlintMsgChannel() {
            @Override
            public void onMessageReceived(FlintDevice flingDevice,
                    String namespace, String message) {
                super.onMessageReceived(flingDevice, namespace, message);

                // show received custom messages.
                Log.d(TAG, "onMessageReceived: " + message);

                ((VideoPlayerActivity) mContext).onReceiveCustMessage(message);
            }
        };
    }

    /**
     * start media route
     */
    public void onStart() {
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    /**
     * stop media route
     */
    public void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    /**
     * Launch receiver application.
     */
    public void launchApplication() {
        if (!mApiClient.isConnected()) {
            return;
        }

        Flint.FlintApi.launchApplication(mApiClient, getAppUrl(), true)
                .setResultCallback(
                        new ApplicationConnectionResultCallback("LaunchApp"));
    }

    /**
     * Join to receiver application.
     */
    public void joinApplication() {
        if (!mApiClient.isConnected()) {
            return;
        }

        Flint.FlintApi.joinApplication(mApiClient, getAppUrl())
                .setResultCallback(
                        new ApplicationConnectionResultCallback(
                                "JoinApplication"));
    }

    /**
     * Leave to receiver application.
     */
    public void leaveApplication() {
        if (!mApiClient.isConnected()) {
            return;
        }

        Flint.FlintApi.leaveApplication(mApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status result) {
                        if (result.isSuccess()) {
                            mAppMetadata = null;
                            detachMediaPlayer();
                            mStatusChangeListener.onLeaveApplication();
                        }
                    }
                });
    }

    /**
     * Stop receiver application.
     */
    public void stopApplication() {
        if (!mApiClient.isConnected()) {
            return;
        }

        Flint.FlintApi.stopApplication(mApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status result) {
                        if (result.isSuccess()) {
                            mAppMetadata = null;
                            detachMediaPlayer();
                            mStatusChangeListener.onStopApplication();
                            // updateButtonStates();
                        }
                    }
                });
    }

    /**
     * Load the media to receiver
     * 
     * @param autoPlay
     */
    public void loadMedia(boolean autoPlay) {
        if (mAppMetadata == null) {
            return;
        }

        if (mMediaInfo == null) {
            MediaMetadata metadata = new MediaMetadata(
                    MediaMetadata.MEDIA_TYPE_MOVIE);
            metadata.putString(MediaMetadata.KEY_TITLE, "Tears Of Steel");

            mMediaInfo = new MediaInfo.Builder(
                    //"http://fling.matchstick.tv/droidream/samples/TearsOfSteel.mp4")
                    "http://fling.infthink.com/droidream/samples/Dengziqi-1.mp4")
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/mp4").setMetadata(metadata).build();
        }
        Log.d(TAG, "playMedia: " + mMediaInfo);

        if (mMediaPlayer == null) {
            Log.e(TAG, "Trying to play a video with no active media session");
            return;
        }

        mMediaPlayer.load(mApiClient, mMediaInfo, autoPlay).setResultCallback(
                new MediaResultCallback(mContext
                        .getString(R.string.mediaop_load)));
    }

    /**
     * Play media
     */
    public void playMedia() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.play(mApiClient).setResultCallback(
                new MediaResultCallback(mContext
                        .getString(R.string.mediaop_play)));
    }

    /**
     * Pause media
     */
    public void pauseMedia() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.pause(mApiClient).setResultCallback(
                new MediaResultCallback(mContext
                        .getString(R.string.mediaop_pause)));
    }

    /**
     * Stop media
     */
    public void stopMedia() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.stop(mApiClient).setResultCallback(
                new MediaResultCallback(mContext
                        .getString(R.string.mediaop_stop)));

    }

    /**
     * Seek media
     * 
     * @param position
     * @param resumeState
     */
    public void seekMedia(long position, int resumeState) {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.seek(mApiClient, position, resumeState).setResultCallback(
                new MediaResultCallback(mContext
                        .getString(R.string.mediaop_seek)) {
                    @Override
                    protected void onFinished() {
                        mStatusChangeListener.onMediaSeekEnd();
                    }
                });
    }

    /**
     * Set device's volume
     * 
     * @param volume
     */
    public void setDeviceVolume(int volume) {
        if (!mApiClient.isConnected()) {
            return;
        }
        try {
            Flint.FlintApi.setVolume(mApiClient, volume / MAX_VOLUME_LEVEL);
        } catch (IOException e) {
            Log.w(TAG, "Unable to change volume");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set device in mute status
     * 
     * @param on
     */
    public void setDeviceMute(boolean on) {
        if (!mApiClient.isConnected()) {
            return;
        }
        try {
            Flint.FlintApi.setMute(mApiClient, on);
        } catch (IOException e) {
            Log.w(TAG, "Unable to toggle mute");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set media's stream volume
     * 
     * @param volume
     */
    public void setMediaVolume(int volume) {
        if (mMediaPlayer == null) {
            return;
        }
        try {
            mMediaPlayer
                    .setStreamVolume(mApiClient, volume / MAX_VOLUME_LEVEL)
                    .setResultCallback(
                            new MediaResultCallback(
                                    mContext.getString(R.string.mediaop_set_stream_volume)));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set media mute
     * 
     * @param on
     */
    public void setMediaMute(boolean on) {
        if (mMediaPlayer == null) {
            return;
        }
        try {
            mMediaPlayer.setStreamMute(mApiClient, on).setResultCallback(
                    new MediaResultCallback(mContext
                            .getString(R.string.mediaop_toggle_stream_mute)) {
                        @Override
                        protected void onFinished() {
                            mStatusChangeListener.onMediaVolumeEnd();
                        }
                    });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Whether device is connected
     * 
     * @return
     */
    public boolean isDeviceConnected() {
        return (mApiClient != null) && mApiClient.isConnected()
                && !mWaitingForReconnect;
    }

    /**
     * Whether receive app is connected.
     * 
     * @return
     */
    public boolean isAppConnected() {
        return (mAppMetadata != null) && !mWaitingForReconnect;
    }

    /**
     * whether media is connected
     * 
     * @return
     */
    public boolean isMediaConnected() {
        return (mMediaPlayer != null) && !mWaitingForReconnect;
    }

    /**
     * Get current media time
     * 
     * @return
     */
    public long getMediaCurrentTime() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getApproximateStreamPosition();
        return 0;
    }

    /**
     * Get current media duration
     * 
     * @return
     */
    public long getMediaDuration() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getStreamDuration();
        return 0;
    }

    /**
     * Get media status
     * 
     * @return
     */
    public MediaStatus getMediaStatus() {
        if (this.mMediaPlayer != null)
            return this.mMediaPlayer.getMediaStatus();
        return null;
    }

    /**
     * Send custom message
     * 
     * @param msg
     *            custom message
     */
    public void sendCustMsg(String msg) {
        mFlintMsgChannel.show(mApiClient, msg);
    }

    /**
     * Get app's url
     * 
     * @return
     */
    private String getAppUrl() {
        return "http://openflint.github.io/flint-player/player.html";
    }

    /**
     * When the user selects a device from the Flint button device list, the
     * application is informed of the selected device by extending
     * MediaRouter.Callback
     * 
     * @author changxing
     * 
     */
    private class FlintMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo route) {
            Log.d(TAG, "onRouteSelected: route=" + route);
            FlintDevice device = FlintDevice.getFromBundle(route.getExtras());
            onDeviceSelected(device);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo route) {
            Log.d(TAG, "onRouteUnselected: route=" + route);
            FlintDevice device = FlintDevice.getFromBundle(route.getExtras());
            onDeviceUnselected(device);
        }
    }

    /**
     * Connect select device
     * 
     * @param device
     */
    private void onDeviceSelected(FlintDevice device) {
        setSelectedDevice(device);

        if (mStatusChangeListener != null)
            mStatusChangeListener.onDeviceSelected(device.getFriendlyName());
    }

    /**
     * Disconnect device
     * 
     * @param device
     */
    private void onDeviceUnselected(FlintDevice device) {
        setSelectedDevice(null);

        if (mStatusChangeListener != null)
            mStatusChangeListener.onDeviceUnselected();
    }

    /**
     * whether select device.
     * 
     * @param device
     */
    private void setSelectedDevice(FlintDevice device) {
        mSelectedDevice = device;

        if (mSelectedDevice == null) {
            detachMediaPlayer();
            stopApplication();
            if ((mApiClient != null) && mApiClient.isConnected()) {
                mApiClient.disconnect();
            }
        } else {
            Log.d(TAG, "acquiring controller for " + mSelectedDevice);
            try {
                Flint.FlintOptions.Builder apiOptionsBuilder = Flint.FlintOptions
                        .builder(mSelectedDevice, mFlintListener);

                mApiClient = new FlintManager.Builder(mContext)
                        .addApi(Flint.API, apiOptionsBuilder.build())
                        .addConnectionCallbacks(mConnectionCallbacks).build();
                mApiClient.connect();
            } catch (IllegalStateException e) {
                Log.w(TAG, "error while creating a device controller", e);
            }
        }
    }

    /**
     * FlintManager.ConnectionCallbacks and
     * FlintManager.OnConnectionFailedListener callbacks to be informed of the
     * connection status. All of the callbacks run on the main UI thread.
     * 
     * @author changxing
     * 
     */
    private class ConnectionCallbacks implements
            FlintManager.ConnectionCallbacks {
        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "ConnectionCallbacks.onConnectionSuspended");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWaitingForReconnect = true;
                    detachMediaPlayer();
                    mStatusChangeListener.onConnectionSuspended();

                }
            });
        }

        @Override
        public void onConnected(final Bundle connectionHint) {
            Log.d(TAG, "ConnectionCallbacks.onConnected");
            if (!mApiClient.isConnected()) {
                return;
            }

            try {
                launchApplication();
            } catch (Exception e) {
                Log.d(TAG, "error requesting status", e);
            }

            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
                if ((connectionHint != null)
                        && connectionHint
                                .getBoolean(Flint.EXTRA_APP_NO_LONGER_RUNNING)) {
                    Log.d(TAG, "App  is no longer running");
                    detachMediaPlayer();
                    mAppMetadata = null;
                    mStatusChangeListener.onNoLongerRunning(false);
                } else {
                    attachMediaPlayer();
                    requestMediaStatus();
                    mStatusChangeListener.onNoLongerRunning(true);
                }
            }

            mStatusChangeListener.onConnected();
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed");
            mStatusChangeListener.onConnectionFailed();
        }
    }

    /**
     * The Flint.Listener callbacks are used to inform the sender application
     * about receiver application events.
     * 
     * @author changxing
     * 
     */
    private class FlintListener extends Flint.Listener {
        @Override
        public void onVolumeChanged() {
            if (mApiClient != null && mApiClient.isConnected()) {
                double volume = Flint.FlintApi.getVolume(mApiClient);
                boolean isMute = Flint.FlintApi.isMute(mApiClient);
                mStatusChangeListener.onVolumeChanged(volume, isMute);
            }
        }

        @Override
        public void onApplicationStatusChanged() {
            if (mApiClient != null && mApiClient.isConnected()) {
                String status = Flint.FlintApi.getApplicationStatus(mApiClient);
                Log.d(TAG, "onApplicationStatusChanged; status=" + status);
                mStatusChangeListener.onApplicationStatusChanged(status);
            }
        }

        @Override
        public void onApplicationDisconnected(int statusCode) {
            Log.d(TAG, "onApplicationDisconnected: statusCode=" + statusCode);
            mAppMetadata = null;
            detachMediaPlayer();
            mStatusChangeListener.onApplicationDisconnected();
            if (statusCode != ConnectionResult.SUCCESS) {
                // This is an unexpected disconnect.
                mStatusChangeListener.onApplicationStatusChanged(mContext
                        .getString(R.string.status_app_disconnected));
            }
        }
    }

    /**
     * To use the media channel create an instance of RemoteMediaPlayer and set
     * the update listeners to receive media status updates.
     */
    private void attachMediaPlayer() {
        if (mMediaPlayer != null) {
            return;
        }

        mMediaPlayer = new RemoteMediaPlayer();
        mMediaPlayer
                .setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {

                    @Override
                    public void onStatusUpdated() {
                        Log.d(TAG, "MediaControlChannel.onStatusUpdated");
                        mStatusChangeListener.onMediaStatusUpdated();
                    }
                });

        mMediaPlayer
                .setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
                    @Override
                    public void onMetadataUpdated() {
                        Log.d(TAG, "MediaControlChannel.onMetadataUpdated");
                        String title = null;
                        String artist = null;
                        Uri imageUrl = null;

                        MediaInfo mediaInfo = mMediaPlayer.getMediaInfo();
                        if (mediaInfo != null) {
                            MediaMetadata metadata = mediaInfo.getMetadata();
                            if (metadata != null) {
                                title = metadata
                                        .getString(MediaMetadata.KEY_TITLE);

                                artist = metadata
                                        .getString(MediaMetadata.KEY_ARTIST);
                                if (artist == null) {
                                    artist = metadata
                                            .getString(MediaMetadata.KEY_STUDIO);
                                }

                                List<WebImage> images = metadata.getImages();
                                if ((images != null) && !images.isEmpty()) {
                                    WebImage image = images.get(0);
                                    imageUrl = image.getUrl();
                                }
                            }
                            mStatusChangeListener.onMediaMetadataUpdated(title,
                                    artist, imageUrl);
                        }
                    }
                });

        try {
            Flint.FlintApi.setMessageReceivedCallbacks(mApiClient,
                    mMediaPlayer.getNamespace(), mMediaPlayer);

            // ready to send/receive custom message.
            Flint.FlintApi.setMessageReceivedCallbacks(mApiClient,
                    mFlintMsgChannel.getNamespace(), mFlintMsgChannel);
        } catch (IOException e) {
            Log.w(TAG, "Exception while launching application", e);
        }
    }

    /**
     * detach media player.
     */
    private void detachMediaPlayer() {
        if ((mMediaPlayer != null) && (mApiClient != null)) {
            try {
                Flint.FlintApi.removeMessageReceivedCallbacks(mApiClient,
                        mMediaPlayer.getNamespace());

                // detach send/receive custom message.
                Flint.FlintApi.removeMessageReceivedCallbacks(mApiClient,
                        mFlintMsgChannel.getNamespace());
            } catch (IOException e) {
                Log.w(TAG, "Exception while detaching media player", e);
            }
        }
        mMediaPlayer = null;
    }

    /**
     * Get current media status.
     */
    private void requestMediaStatus() {
        if (mMediaPlayer == null) {
            return;
        }

        Log.d(TAG, "requesting current media status");
        mMediaPlayer.requestStatus(mApiClient).setResultCallback(
                new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                    @Override
                    public void onResult(MediaChannelResult result) {
                        Status status = result.getStatus();
                        if (!status.isSuccess()) {
                            Log.w(TAG,
                                    "Unable to request status: "
                                            + status.getStatusCode());
                        }
                    }
                });
    }

    /**
     * Used to indicate the status of application connection.
     * 
     * @author jim
     *
     */
    private final class ApplicationConnectionResultCallback implements
            ResultCallback<Flint.ApplicationConnectionResult> {
        private final String mClassTag;

        public ApplicationConnectionResultCallback(String suffix) {
            mClassTag = TAG + "_" + suffix;
        }

        @Override
        public void onResult(ApplicationConnectionResult result) {
            Status status = result.getStatus();
            Log.d(mClassTag,
                    "ApplicationConnectionResultCallback.onResult: statusCode"
                            + status.getStatusCode());
            if (status.isSuccess()) {
                ApplicationMetadata applicationMetadata = result
                        .getApplicationMetadata();
                String applicationStatus = result.getApplicationStatus();
                // setApplicationStatus(applicationStatus);
                attachMediaPlayer();
                mAppMetadata = applicationMetadata;
                requestMediaStatus();
                mStatusChangeListener
                        .onApplicationConnectionResult(applicationStatus);
            }
        }
    }

    /**
     * Current media result status.
     */
    private class MediaResultCallback implements
            ResultCallback<MediaChannelResult> {
        private final String mOperationName;

        public MediaResultCallback(String operationName) {
            mOperationName = operationName;
        }

        @Override
        public void onResult(MediaChannelResult result) {
            Status status = result.getStatus();
            if (!status.isSuccess()) {
                Log.w(TAG,
                        mOperationName + " failed: " + status.getStatusCode());
            }
            onFinished();
        }

        protected void onFinished() {
        }
    }
    
    /**
     * Set custom message to device. let device use hardware decoder or not
     * 
     * @param flag
     */
    public void setHardwareDecoder(boolean flag) {
        if (mApiClient == null || !mApiClient.isConnected()) {
            return;
        }
        
        mFlintMsgChannel.setHardwareDecoder(mApiClient, flag);
    }
}
