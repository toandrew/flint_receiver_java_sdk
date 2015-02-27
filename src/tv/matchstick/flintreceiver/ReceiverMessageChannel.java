package tv.matchstick.flintreceiver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import android.util.Log;

/**
 * Receiver's message channel.
 * 
 * @author jim
 *
 */
public class ReceiverMessageChannel extends MessageChannel {
    private static final String TAG = "ReceiverMessageChannel";

    private HashMap<String, String> mSenders = new HashMap<String, String>();

    private static final String MSG_DATA = "data";
    private static final String MSG_DATA_TYPE = "type";

    private static final String MSG_DATA_SENDERCONNECTED = "senderConnected";
    private static final String MSG_DATA_SENDERDISCONNECTED = "senderDisconnected";

    private static final String MSG_DATA_MESSAGE = "message";
    private static final String MSG_DATA_ERROR = "error";
    private static final String MSG_DATA_DATA = "data";
    private static final String MSG_DATA_SENDERID = "senderId";
    private static final String MSG_DATA_NAMESPACE = "namespace";

    public ReceiverMessageChannel(String name, String url) {
        super(name, url);
        // TODO Auto-generated constructor stub

        mSenders.clear();
    }

    @Override
    public void send(String data) {
        super.send(data);
    }

    @Override
    public HashMap<String, String> getSenders() {
        // TODO Auto-generated method stub

        return mSenders;
    }

    @Override
    public void onOpen(String data) {
        // TODO Auto-generated method stub

        super.onOpen(data);
    }

    @Override
    public void onClose(String data) {
        // TODO Auto-generated method stub

        super.onClose(data);
    }

    @Override
    public void onError(String data) {
        // TODO Auto-generated method stub

        super.onError(data);
    }

    @Override
    public void onMessage(String data) {
        // TODO Auto-generated method stub

        super.onMessage(data);

        try {
            JSONObject d = new JSONObject(data);

            //JSONObject d = message.getJSONObject(MSG_DATA);

            String type = d.getString(MSG_DATA_TYPE);
            if (type.equals(MSG_DATA_SENDERCONNECTED)) {
                mSenders.put(MSG_DATA_SENDERID, d.getString(MSG_DATA_SENDERID));

                String senderId = d.getString(MSG_DATA_SENDERID);
                onSenderConnected(senderId);
            } else if (type.equals(MSG_DATA_SENDERDISCONNECTED)) {
                mSenders.remove(d.getString(MSG_DATA_SENDERID));

                String senderId = d.getString(MSG_DATA_SENDERID);
                onSenderDisConnected(senderId);
            } else if (type.equals(MSG_DATA_MESSAGE)) {
                String m = d.getString(MSG_DATA_DATA);

                String senderId = d.getString(MSG_DATA_SENDERID);
                onMessageReceived(m, senderId);
            } else if (type.equals(MSG_DATA_ERROR)) {
                String ex = d.getString(MSG_DATA_MESSAGE);
                onErrorHappened(ex);
            } else {
                Log.e(TAG, "ReceiverMessageChannel unknow data.type:" + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when Sender App connected.
     * 
     * @param senderId
     */
    public void onSenderConnected(String senderId) {
        try {
            Iterator<Entry<String, MessageBus>> iter = mMessageBusMap
                    .entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                        .next();
                MessageBus bus = (MessageBus) entry.getValue();
                bus.onSenderConnected(senderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when Sender App disconnected.
     * 
     * @param senderId
     */
    public void onSenderDisConnected(String senderId) {
        try {
            Iterator<Entry<String, MessageBus>> iter = mMessageBusMap
                    .entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                        .next();
                MessageBus bus = (MessageBus) entry.getValue();
                bus.onSenderDisConnected(senderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when message received
     * 
     * @param data
     * @param senderId
     */
    public void onMessageReceived(String data, String senderId) {
        try {
            JSONObject json = new JSONObject(data);
            String namespace = json.getString(NAMESPACE);

            Iterator<Entry<String, MessageBus>> iter = mMessageBusMap
                    .entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                        .next();

                String ns = (String) entry.getKey();

                if (ns.equals(namespace)) {
                    MessageBus bus = (MessageBus) entry.getValue();
                    bus.onMessageReceived(data, senderId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when error happened.
     * 
     * @param ex
     */
    public void onErrorHappened(String ex) {
        try {
            Iterator<Entry<String, MessageBus>> iter = mMessageBusMap
                    .entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, MessageBus> entry = (Map.Entry<String, MessageBus>) iter
                        .next();

                MessageBus bus = (MessageBus) entry.getValue();
                bus.onErrorHappened(ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
