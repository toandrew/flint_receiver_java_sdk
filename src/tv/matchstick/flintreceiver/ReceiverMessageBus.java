package tv.matchstick.flintreceiver;

import java.util.HashMap;

import org.json.JSONObject;

import android.util.Log;

/**
 * This is used to transfer messages between sender and receiver Apps.
 * 
 * @author jim
 *
 */
public class ReceiverMessageBus extends MessageBus {
    private static final String TAG = "ReceiverMessageBus";

    private static final String PAYLOAD = "payload";

    ReceiverMessageBus(MessageChannel channel, String namespace) {
        super(channel, namespace);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void send(String data, String senderId) {
        // TODO Auto-generated method stub
    }

    @Override
    public HashMap<String, String> getSenders() {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSenderConnected(String senderId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSenderDisConnected(String senderId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessageReceived(String data, String senderId) {
        // TODO Auto-generated method stub

        try {
            JSONObject json = new JSONObject(data);
            String payload = json.getString(PAYLOAD);

            onPayloadMessage(payload, senderId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorHappened(String ex) {
        // TODO Auto-generated method stub

    }

    /**
     * Received valid payload message
     * 
     * @param payload
     * @param senderId
     */
    public void onPayloadMessage(String payload, String senderId) {
        Log.d(TAG, "Received payload[" + payload + "]senderId[" + senderId
                + "]");
    }
}
