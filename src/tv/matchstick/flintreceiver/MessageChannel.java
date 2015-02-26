package tv.matchstick.flintreceiver;

import java.util.ArrayList;

/**
 * Interact messages with Fling daemon and Sender Apps
 * 
 * @author jim
 *
 */
public abstract class MessageChannel {
    private boolean mIsOpened;

    private String mName;

    public MessageChannel(String name, String url) {
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
        return true;
    }

    /**
     * Close the message channel.
     * 
     * @return
     */
    public boolean close() {
        return true;
    }

    /**
     * Send messages
     * 
     * @param data
     */
    public void send(String data) {
    }

    public ArrayList<String> getSenders() {
        return null;
    }
    
    public abstract void onOpen(String data);
    public abstract void onClose(String data);
    public abstract void onError(String data);
    public abstract void onMessage(String data);
}
