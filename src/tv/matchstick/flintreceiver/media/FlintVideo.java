package tv.matchstick.flintreceiver.media;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Flint Video element
 * 
 * Use this class to interact with Video related.
 * 
 * @author jim
 *
 */
public class FlintVideo {

    // video events
    public static final String EMPTIED = "emptied";

    public static final String LOADEDMETADATA = "loadedmetadata";

    public static final String PLAY = "play";

    public static final String PLAYING = "playing";

    public static final String WAITING = "waiting";

    public static final String PAUSE = "pause";

    public static final String ENDED = "ended";

    public static final String VOLUMECHANGE = "volumechange";

    public static final String SEEKED = "seeked";

    public static final String CANPLAY = "canplay";

    public static final String ERROR = "error";

    public static final String ABORT = "abort";

    // IDLE REASONEs
    public static final String IDLE_REASON_NONE = "NONE";

    public static final String IDLE_REASON_FINISHED = "FINISHED";

    public static final String IDLE_REASON_ERROR = "ERROR";

    public static final String IDLE_REASON_INTERRUPTED = "INTERRUPTED";

    public static final String IDLE_REASON_CANCELLED = "CANCELLED";

    HashMap<String, Callback> mListeners;

    private int mVolume = 0;

    private double mDuration = 0;

    private double mPlaybackRate = 0;

    private double mCurrentTime = 0;

    private boolean mMuted = false;

    private String mUrl;

    private boolean mAutoPlay = true;

    public FlintVideo() {
        mListeners = new HashMap<String, Callback>();
    }

    /**
     * Add media event listener
     * 
     * @param event
     * @param callback
     */
    public void addEventListener(String event, Callback callback) {
        mListeners.put(event, callback);
    }

    /**
     * Delete media event listener.
     * 
     * @param event
     * @param callback
     */
    public void removeEventListener(String event, Callback callback) {
        mListeners.remove(event);
    }

    /**
     * Set current volume
     * 
     * @param volume
     * @return
     */
    public void setVolume(int volume) {
        mVolume = volume;
    }

    /**
     * Get current media volume
     * 
     * @return
     */
    public int getVolume() {
        return mVolume;
    }

    /**
     * Get video's duration
     * 
     * @return
     */
    public double getDuration() {
        return mDuration;
    }

    /**
     * Get video's playback rate.
     * 
     * @return
     */
    public double getPlaybackRate() {
        return mPlaybackRate;
    }

    /**
     * Set current video's play position
     * 
     * @param time
     */
    public void setCurrentTime(double time) {
        mCurrentTime = time;
    }

    /**
     * Get current Time
     * 
     * @return
     */
    public double getCurrentTime() {
        return mCurrentTime;
    }

    /**
     * Whether video is muted
     * 
     * @return
     */
    public boolean isMuted() {
        return mMuted;
    }

    /**
     * Set play url
     * 
     * @param url
     */
    public void setUrl(String url) {
        mUrl = url;
    }

    /**
     * Get play url
     * 
     * @return
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Ready to load video.
     */
    public void load() {
    }

    /**
     * Set whether autoplay
     * 
     * @param autoplay
     */
    public void setAutoPlay(boolean autoplay) {
        mAutoPlay = autoplay;
    }

    /**
     * Whether autoplay is enabled.
     * 
     * @return
     */
    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    /**
     * Pause video.
     */
    public void pause() {

    }

    /**
     * Play video.
     */
    public void play() {

    }

    /**
     * Stop video.
     */
    public void stop() {

    }

    static abstract class Callback {
        public abstract void process();
    }

    static class FlintMediaMetadata {
        public FlintMedia media;
    }

    static class FlintMedia {
        public String streamType;
        public String contentType;
        public String contentId;
        public FlintMetadata metadata;
    }

    static class FlintMetadata {
        public String title;
        public String subtitle;
        public ArrayList<String> images;
        public String metadataType;
    }
}
