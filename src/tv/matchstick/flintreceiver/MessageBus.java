package tv.matchstick.flintreceiver;

import java.util.ArrayList;

/**
 * This is used to transfer messages between sender and receiver Apps.
 * 
 * @author jim
 *
 */
public abstract class MessageBus {
    private static final String TAG = "MessageBus";

    MessageBus(MessageChannel channel, String namespace) {
    }

    public abstract void send(String data, String senderId);

    public abstract ArrayList<String> getSenders();
    
    public void close() {
        
    }
}
