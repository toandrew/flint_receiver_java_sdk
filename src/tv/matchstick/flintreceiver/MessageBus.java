package tv.matchstick.flintreceiver;

import java.util.HashMap;

/**
 * This is used to transfer messages between sender and receiver Apps.
 * 
 * @author jim
 *
 */
public abstract class MessageBus implements FlintWebSocketListener {
    private static final String TAG = "MessageBus";

    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_MEDIA = "media";
    
    MessageChannel mMessageChannel;
    String mNamespace;

    public MessageBus(String namespace) {
        mNamespace = namespace;

        init();
    }

    /**
     * Set related message channel.
     * 
     * @param channel
     */
    public void setMessageChannel(MessageChannel channel) {
        mMessageChannel = channel;
        mMessageChannel.registerMessageBus(this, mNamespace);
    }
    
    public abstract void init();
    
    public abstract void send(String data, String senderId);

    public abstract HashMap<String, String> getSenders();

    public void close() {
        if (mMessageChannel != null) {
            mMessageChannel.unRegisterMessageBus(mNamespace);
        }
    }

    /**
     * Called when Sender App connected.
     * 
     * @param senderId
     */
    public abstract void onSenderConnected(String senderId);

    /**
     * Called when Sender App disconnected.
     * 
     * @param senderId
     */
    public abstract void onSenderDisconnected(String senderId);

    /**
     * Called when raw message received
     * 
     * @param data
     * @param senderId
     */
    public abstract void onMessageReceived(String data, String senderId);

    /**
     * Called when error happened.
     * 
     * @param ex
     */
    public abstract void onErrorHappened(String ex);

    @Override
    public void onOpen(String data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClose(String data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onError(String data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMessage(String data) {
        // TODO Auto-generated method stub
        
    }
}
