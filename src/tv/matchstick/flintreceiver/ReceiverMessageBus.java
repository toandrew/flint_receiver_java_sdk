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
public abstract class ReceiverMessageBus extends MessageBus {
    private static final String TAG = "ReceiverMessageBus";

    private static final String PAYLOAD = "payload";
    private static final String NAMESPACE = "namespace";
    private static final String DATA = "data";
    private static final String SENDERID = "senderId";
    
    HashMap<String, String> mSenders = new HashMap<String, String>();

    protected ReceiverMessageBus(String namespace) {
        super(namespace);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(String data, String senderId) {
        // TODO Auto-generated method stub

        if (data == null) {
            Log.e(TAG, "data is null!ignore send!");
            return;
        }
        try {
            JSONObject message = new JSONObject();
            message.put(NAMESPACE, mNamespace);
            message.put(PAYLOAD, data);

            JSONObject obj = new JSONObject();

            if (senderId == null) {
                obj.put(SENDERID, "*.*"); // all
            } else {
                obj.put(SENDERID, senderId);
            }
            obj.put(DATA, message.toString());

            Log.e(TAG, "send[" + obj.toString() + "]");
            
            mMessageChannel.send(obj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<String, String> getSenders() {
        // TODO Auto-generated method stub

        return mSenders;
    }

    @Override
    public void onSenderConnected(String senderId) {
        // TODO Auto-generated method stub
        
        mSenders.put(senderId, senderId);
    }

    @Override
    public void onSenderDisconnected(String senderId) {
        // TODO Auto-generated method stub
        
        mSenders.remove(senderId);
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
    public abstract void onPayloadMessage(String payload, String senderId);
}
