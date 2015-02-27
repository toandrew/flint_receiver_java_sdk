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

    MessageChannel mMessageChannel;
    String mNamespace;

    MessageBus(MessageChannel channel, String namespace) {
        mMessageChannel = channel;
        mNamespace = namespace;

        mMessageChannel.registerMessageBus(this, mNamespace);

        init();
    }

    public abstract void send(String data, String senderId);

    public abstract void init();

    public abstract HashMap<String, String> getSenders();

    public void close() {
        mMessageChannel.unRegisterMessageBus(mNamespace);
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
    public abstract void onSenderDisConnected(String senderId);

    /**
     * Called when message received
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
