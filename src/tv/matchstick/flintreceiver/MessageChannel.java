package tv.matchstick.flintreceiver;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

/**
 * Interact messages with Fling daemon and Sender Apps
 * 
 * @author jim
 *
 */
public abstract class MessageChannel implements FlintWebSocketListener {
    private static final String TAG = "MessageChannel";

    public static final String NAMESPACE = "namespace";

    private boolean mIsOpened = false;

    private FlintWebSocket mWebSocket = null;

    protected HashMap<String, MessageBus> mMessageBusMap = new HashMap<String, MessageBus>();

    private String mName;

    private String mUrl;

    public MessageChannel(String name, String url) {
        mName = name;
        mUrl = url;
    }

    /**
     * Whether the message channel is opened.
     * 
     * @return
     */
    public boolean isOpened() {
        return mIsOpened;
    }

    /**
     * Get the message channel's name
     * 
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Open the message channel.
     * 
     * @return
     */
    public boolean open() {
        return open(null);
    }

    /**
     * Open the message channel.
     * 
     * @param url
     * @return
     */
    public boolean open(String url) {
        if (url != null) {
            mUrl = url;
        }
        
        Log.e(TAG, "open: url[" + mUrl + "]");
        
        mWebSocket = new FlintWebSocket(this, URI.create(mUrl));
        mWebSocket.connect();

        mIsOpened = true;

        return true;
    }

    /**
     * Close the message channel.
     * 
     * @return
     */
    public boolean close() {
        mIsOpened = false;

        if (mWebSocket != null) {
            mWebSocket.close();
        }

        return true;
    }

    /**
     * Send messages
     * 
     * @param data
     */
    public void send(final String data) {
        if (!mIsOpened) {
            Log.e(TAG, "MessageChannel is not opened, cannot sent:" + data);
            return;
        }

        if (data == null) {
            return;
        }

        if (mWebSocket.isOpen()) {
            mWebSocket.send(data);
        } else if (mWebSocket.isConnecting()) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    send(data);
                }

            };

            timer.schedule(task, 50);
        } else {
            Log.e(TAG, "MessageChannel send failed, channel readyState is:"
                    + mWebSocket.getReadyState());
        }
    }

    /**
     * Get Senders
     * 
     * @return
     */
    public HashMap<String, String> getSenders() {
        return null;
    }

    /**
     * Register MessageBus
     * 
     * @param messageBus
     * @param namespace
     */
    public void registerMessageBus(MessageBus messageBus, String namespace) {
        if (messageBus != null && namespace != null) {
            mMessageBusMap.put(namespace, messageBus);
        }
    }

    /**
     * Unregister MessageBus
     * 
     * @param namespace
     */
    public void unRegisterMessageBus(String namespace) {
        if (namespace != null) {
            mMessageBusMap.remove(namespace);
        }
    }

    /**
     * Notify all MessageBus obj about open events
     */
    public void onOpen(String data) {
        Iterator<Entry<String, MessageBus>> iter = mMessageBusMap.entrySet()
                .iterator();
        while (iter.hasNext()) {
            Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                    .next();
            MessageBus bus = (MessageBus) entry.getValue();
            bus.onOpen(data);
        }
    }

    /**
     * Notify all MessageBus obj about close event
     */
    public void onClose(String data) {
        Iterator<Entry<String, MessageBus>> iter = mMessageBusMap.entrySet()
                .iterator();
        while (iter.hasNext()) {
            Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                    .next();
            MessageBus bus = (MessageBus) entry.getValue();
            bus.onClose(data);
        }
    }

    /**
     * Notify all MessageBus obj about error event
     */
    public void onError(String data) {
        Iterator<Entry<String, MessageBus>> iter = mMessageBusMap.entrySet()
                .iterator();
        while (iter.hasNext()) {
            Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                    .next();
            MessageBus bus = (MessageBus) entry.getValue();
            bus.onError(data);
        }
    }

    /**
     * Notify all MessageBus obj about message received event
     */
    public void onMessage(String data) {
        Log.d(TAG, "onMessage:" + data);
    }
}
