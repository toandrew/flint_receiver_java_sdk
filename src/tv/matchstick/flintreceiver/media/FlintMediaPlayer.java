package tv.matchstick.flintreceiver.media;

import java.util.Timer;
import java.util.TimerTask;

import tv.matchstick.flintreceiver.FlintConstants;
import tv.matchstick.flintreceiver.FlintReceiverManager;
import tv.matchstick.flintreceiver.MessageBus;
import tv.matchstick.flintreceiver.media.FlintVideo.FlintMediaMetadata;

/**
 * Flint default media player which can be used to communicate media events with
 * Media Sender Apps.
 * 
 * @author jim
 *
 */
public class FlintMediaPlayer {
    private static final String TAG = "FlintMediaPlayer";

    public static final String PLAYER_STATE_IDLE = "IDLE";
    public static final String PLAYER_STATE_PLAYING = "PLAYING";
    public static final String PLAYER_STATE_PAUSED = "PAUSED";
    public static final String PLAYER_STATE_BUFFERING = "BUFFERING";
    public static final String PLAYER_STATE_LOADDING = "LOADDING";
    public static final String PLAYER_STATE_READY = "READY";

    private String mStatus = PLAYER_STATE_IDLE;

    private String mPlayerState = PLAYER_STATE_IDLE;

    private String mTitle;

    private String mSubtitle;

    private String mUrl;

    private FlintMediaMetadata mMediaMetadata;

    private int mVideoVolume;

    private int mRequestId = 0;

    private int mRequestIdLoad = 0;

    private int mRequestIdPause = 0;

    private int mRequestIdPlay = 0;

    private int mRequestIdSetVolume = 0;

    private int mRequestIdSeek = 0;

    private int mRequestIdGetStatus = 0;

    private int mrequestIdStop = 0;

    private String mBroadcastSenderId = "*:*";

    /**
     * Flint receiver manager
     */
    private FlintReceiverManager mReceiverManager;

    /**
     * Message reporter.
     */
    private MessageReport mMessageReport;

    /**
     * Flint video element.
     */
    private final FlintVideo mFlintVideo;

    /**
     * Message bus
     */
    private MessageBus mMessageBus;

    /**
     * Flint Media Player
     * 
     * @param manager
     * @param video
     */
    public FlintMediaPlayer(FlintReceiverManager manager, FlintVideo video) {
        mReceiverManager = manager;

        mFlintVideo = video;

        mMessageReport = new MessageReport();

        mMessageBus = mReceiverManager
                .createMessageBus(FlintConstants.MEDIA_NAMESPACE);

        init();
    }

    private void init() {
        mFlintVideo.addEventListener(FlintVideo.EMPTIED,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.idle(FlintVideo.IDLE_REASON_NONE);
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.LOADEDMETADATA,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mStatus = "READY";

                        mMessageReport.loadmetadata();
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.PLAY,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.playing();
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.PLAYING,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.playing();
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.WAITING,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.buffering();
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.PAUSE,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.paused();
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.ENDED,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.idle(FlintVideo.IDLE_REASON_FINISHED);
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.VOLUMECHANGE,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mVideoVolume = mFlintVideo.getVolume();

                        mMessageReport
                                .syncPlayerState(MessageReport.SYNC_TYPE_VOLUMECHANGE);
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.SEEKED,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport
                                .syncPlayerState(MessageReport.SYNC_TYPE_SEEKED);
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.CANPLAY,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport
                                .syncPlayerState(MessageReport.SYNC_TYPE_OTHERS);
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.ERROR,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.idle(FlintVideo.IDLE_REASON_ERROR);
                    }

                });

        mFlintVideo.addEventListener(FlintVideo.ABORT,
                new FlintVideo.Callback() {

                    @Override
                    public void process() {
                        // TODO Auto-generated method stub

                        mMessageReport.idle(FlintVideo.IDLE_REASON_INTERRUPTED);
                    }

                });

    }

    /**
     * Process "LOAD" command
     * 
     * @param url
     * @param videoType
     * @param title
     * @param subtitle
     * @param mediaMetadata
     */
    public void load(String url, String videoType, String title,
            String subtitle, FlintMediaMetadata mediaMetadata) {
        mMediaMetadata = mediaMetadata;

        mTitle = title;

        mSubtitle = subtitle;

        mUrl = url;

        mFlintVideo.setUrl(mUrl);

        mFlintVideo.load();

        mFlintVideo.setAutoPlay(true);
    }

    /**
     * Process PAUSE
     */
    public void pause() {
        syncExecute(new FlintVideo.Callback() {

            @Override
            public void process() {
                // TODO Auto-generated method stub
                mFlintVideo.pause();
            }

        });
    }

    /**
     * Process PLAY
     */
    public void play() {
        syncExecute(new FlintVideo.Callback() {

            @Override
            public void process() {
                // TODO Auto-generated method stub
                mFlintVideo.play();
            }

        });
    }

    /**
     * Process SEEK
     */
    public void seek(final double time) {
        syncExecute(new FlintVideo.Callback() {

            @Override
            public void process() {
                // TODO Auto-generated method stub
                mFlintVideo.setCurrentTime(time);
            }

        });
    }

    /**
     * Process change volume
     */
    public void changeVolume(final int volume) {
        syncExecute(new FlintVideo.Callback() {

            @Override
            public void process() {
                // TODO Auto-generated method stub
                mFlintVideo.setVolume(volume);
            }
        });

        // TODO
    }

    /**
     * Process STOP
     */
    public void stop() {
        syncExecute(new FlintVideo.Callback() {

            @Override
            public void process() {
                // TODO Auto-generated method stub
                mFlintVideo.stop();
            }
        });

        // TODO

        mMessageReport.idle(FlintVideo.IDLE_REASON_CANCELLED);
    }

    /**
     * Sync exec method.
     */
    private void syncExecute(final FlintVideo.Callback readyCallBack) {
        if (mStatus.equals(PLAYER_STATE_READY)) {
            readyCallBack.process();
            return;
        }

        if (mStatus.equals(PLAYER_STATE_IDLE)) {
            return;
        }

        if (mStatus.equals(PLAYER_STATE_LOADDING)) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    syncExecute(readyCallBack);
                }

            };

            timer.schedule(task, 50);

            return;
        }
    }

    private class MessageReport {
        /**
         * Fired when volume changed
         */
        public static final String SYNC_TYPE_VOLUMECHANGE = "volumechange";

        /**
         * Fired when media seeked.
         */
        public static final String SYNC_TYPE_SEEKED = "seeked";

        /**
         * Fired when sync other media events
         */
        public static final String SYNC_TYPE_OTHERS = "others";

        /**
         * Process IDLE
         */
        public void idle(String reason) {
            String messageData = loadData(PLAYER_STATE_IDLE, reason, null, null);

            mMessageBus.send(messageData, mBroadcastSenderId);
        }

        /**
         * Process LOAD META DATA.
         */
        public void loadmetadata() {
            String messageData = loadData(PLAYER_STATE_PLAYING, null,
                    mRequestIdLoad + "", mMediaMetadata);

            mMessageBus.send(messageData, mBroadcastSenderId);
        }

        /**
         * Process PLAYING
         */
        public void playing() {
            String messageData = loadData(PLAYER_STATE_PLAYING, null,
                    mRequestIdPlay + "", null);

            mMessageBus.send(messageData, mBroadcastSenderId);
        }

        /**
         * Process PAUSED.
         */
        public void paused() {
            String messageData = loadData(PLAYER_STATE_PAUSED, null,
                    mRequestIdPause + "", null);

            mMessageBus.send(messageData, mBroadcastSenderId);
        }

        /**
         * Process BUFFERING
         * 
         * @return
         */
        public void buffering() {
            String messageData = loadData(PLAYER_STATE_BUFFERING, null, null,
                    null);

            mMessageBus.send(messageData, mBroadcastSenderId);
        }

        /**
         * Process "SYNC PLAYER STATE"
         * 
         * @return
         */
        public void syncPlayerState(String type) {
            String messageData = null;

            if (type.equals(FlintVideo.SEEKED)) {
                messageData = loadData(mPlayerState, null, mRequestIdSeek + "",
                        mMediaMetadata);
            } else if (type.equals(FlintVideo.VOLUMECHANGE)) {
                messageData = loadData(mPlayerState, null, mRequestIdSetVolume
                        + "", mMediaMetadata);
            } else {
                messageData = loadData(mPlayerState, null, null, mMediaMetadata);
            }

            mMessageBus.send(messageData, mBroadcastSenderId);
        }

        /**
         * Load data
         * 
         * @return
         */
        private String loadData(String playerState, String idleReason,
                String requestId, FlintMediaMetadata metadata) {
            String data = "{\"type\": \"MEDIA_STATUS\", " + "\"status\": ["
                    + "{" + "\"mediaSessionId\": 1," + "\"playbackRate\": "
                    + mFlintVideo.getPlaybackRate()
                    + ","
                    + "\"currentTime\": "
                    + mFlintVideo.getCurrentTime()
                    + ","
                    + "\"duration\": "
                    + mFlintVideo.getDuration()
                    + ","
                    + "\"supportedMediaCommands\": 15,"
                    + "\"volume\": {"
                    + "\"level\":"
                    + mFlintVideo.getVolume()
                    + ","
                    + "\"muted\": "
                    + mFlintVideo.isMuted()
                    + "}"
                    + (playerState != null ? "\"playerState\": " + playerState
                            + "," : "")
                    + (idleReason != null ? "\"idleReason\": " + idleReason
                            + "," : "")
                    + (metadata != null ? getMediaData(metadata) : "")
                    + "}"
                    + "]"
                    + "\"requestId\": "
                    + (requestId != null ? requestId : "0") + "}";

            return data;
        }

        private String getMediaData(FlintMediaMetadata mediaMetadata) {
            String data = "\"media\": {" + "\"streamType\": "
                    + mediaMetadata.media.streamType + "," + "\"duration\": "
                    + mFlintVideo.getDuration() + ", " + "\"contentType\": "
                    + mediaMetadata.media.contentType + "," + "\"contentId\": "
                    + mediaMetadata.media.contentId + "," + "\"metadata\": {"
                    + "\"title\": " + mediaMetadata.media.metadata.title + ","
                    + "\"subtitle\": " + mediaMetadata.media.metadata.subtitle
                    + "," + "\"images\": "
                    + mediaMetadata.media.metadata.images + ","
                    + "\"metadataType\": "
                    + mediaMetadata.media.metadata.metadataType + "}";
            return data;
        }
    }
}
