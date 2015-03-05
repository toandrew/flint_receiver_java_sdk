package tv.matchstick.flintreceiver;

import android.util.Log;

/**
 * Log utils
 * 
 * @author jim
 *
 */
public class FlintLogger {
    private static boolean DEBUG = true;

    private final String TAG;

    public FlintLogger(String tag, boolean enable) {
        TAG = tag;
        DEBUG = enable;
    }

    public FlintLogger(String tag) {
        this(tag, DEBUG);
    }

    /**
     * Log.v
     * 
     * @param message
     */
    public void v(String message) {
        if (!FlintReceiverManager.isLogEnabled()) {
            return;
        }

        Log.v(TAG, message);
    }

    /**
     * Log.d
     * 
     * @param message
     */
    public void d(String message) {
        if (!FlintReceiverManager.isLogEnabled() || !DEBUG) {
            return;
        }

        Log.d(TAG, message);
    }

    /**
     * Log.i
     * 
     * @param message
     */
    public void i(String message) {
        if (!FlintReceiverManager.isLogEnabled() || !DEBUG) {
            return;
        }

        Log.i(TAG, message);
    }

    /**
     * Log.w
     * 
     * @param message
     */
    public void w(String message) {
        if (!FlintReceiverManager.isLogEnabled() || !DEBUG) {
            return;
        }

        Log.w(TAG, message);
    }

    /**
     * Log.e
     * 
     * @param message
     */
    public void e(String message) {
        if (!FlintReceiverManager.isLogEnabled() || !DEBUG) {
            return;
        }

        Log.e(TAG, message);
    }
}
