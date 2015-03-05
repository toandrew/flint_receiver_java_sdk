package tv.matchstick.flintreceiver;

import java.util.HashMap;

/**
 * This is used to transfer messages between sender and receiver Apps.
 * 
 * @author jim
 *
 */
public abstract class MessageBus implements FlintWebSocketListener {
    MessageChannel mMessageChannel;

    String mNamespace;

    public MessageBus(String namespace) {
        mNamespace = namespace;

        init();
    }

    /**
     * Set the relationship with the message channel.
     * 
     * @param channel
     */
    public void setMessageChannel(MessageChannel channel) {
        mMessageChannel = channel;
        mMessageChannel.registerMessageBus(this, mNamespace);
    }

    /**
     * Do some clean work
     */
    public void unSetMessageChannel() {
        if (mMessageChannel != null) {
            mMessageChannel.unRegisterMessageBus(mNamespace);
        }
    }

    abstract public void init();

    abstract public void send(String data, String senderId);

    abstract public HashMap<String, String> getSenders();

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
