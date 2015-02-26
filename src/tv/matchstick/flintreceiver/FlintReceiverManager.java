package tv.matchstick.flintreceiver;

import java.util.HashMap;

import org.json.JSONObject;

import android.util.Log;

/**
 * Flint Receiver Manager
 * 
 * This is used by java receiver Apps to interact with Flint daemon and Sender
 * Apps
 * 
 * @author jim
 *
 */
public class FlintReceiverManager {
    private static final String TAG = "FlintReceiverManager";

    private static final String IPC_CHANNEL_NAME = "ipc";

    private static final String IPC_MESSAGE_TYPE = "type";

    private static final String IPC_MESSAGE_STARTHEARTBEAT = "startHeartbeat";
    private static final String IPC_MESSAGE_REGISTEROK = "registerok";
    private static final String IPC_MESSAGE_HEARTBEAT = "heartbeat";
    private static final String IPC_MESSAGE_SENDERCONNECTED = "senderconnected";
    private static final String IPC_MESSAGE_SENDERDISCONNECTED = "senderdisconnected";

    private static final String IPC_MESSAGE_DATA_TOKEN = "token";
    private static final String IPC_MESSAGE_DATA_HEARTBEAT = "heartbeat";
    private static final String IPC_MESSAGE_DATA_HEARTBEAT_PING = "ping";
    private static final String IPC_MESSAGE_DATA_HEARTBEAT_PONG = "pong";

    private String mAppId;

    private MessageChannel mIpcChannel;

    private MessageChannel mMessageChannel;

    private String mIpcAddress;

    private String FlintServerIp = "127.0.0.1";

    private HashMap<String, MessageBus> mMessageBusList = new HashMap<String, MessageBus>();

    private String mCustAdditionalData;

    public FlintReceiverManager(String appId) {
        mAppId = appId;

        mIpcAddress = "ws://127.0.0.1:9431/receiver/" + appId;
    }

    /**
     * Ready to start receive messages from Flint daemon and Sender Apps
     * 
     * @return the result
     */
    public boolean open() {
        if (isOpened()) {
            Log.e(TAG, "FlintReceiverManager is already opened!");
            return true;
        }

        mIpcChannel = new MessageChannel(IPC_CHANNEL_NAME, mIpcAddress) {

            @Override
            public void onOpen(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "ipcChannel opened!!!");

                // send register message
                ipcSend("type: 'register'");
            }

            @Override
            public void onClose(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "ipcChannel closed!!!");
            }

            @Override
            public void onError(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "ipcChannel error!!!");
            }

            @Override
            public void onMessage(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "ipcChannel received message: [" + data + "]");
                try {
                    onIpcMessage(new JSONObject(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        mIpcChannel.open();

        if (mMessageChannel != null) {
            mMessageChannel.open();
        }

        return true;
    }

    /**
     * Stop to receive messages from Flint Daemon and Sender Apps
     * 
     * @return the result
     */
    public boolean close() {
        if (isOpened()) {
            ipcSend("type: 'unregister'");

            if (mMessageChannel != null) {
                mMessageChannel.close();
            }

            if (mMessageBusList != null) {
                for (int i = 0; i < mMessageBusList.size(); i++) {
                    MessageBus messageBus = mMessageBusList.get(i);
                    messageBus.close();
                }

                mMessageBusList = null;
            }

            if (mIpcChannel != null) {
                mIpcChannel.close();
            }

            return true;
        }

        Log.e(TAG, "FlintReceiverManager is not started, cannot close!!!");

        return false;
    }

    /**
     * Used to create message bus objects.
     * 
     * @param namespace
     *            the message bus's name
     * @return
     */
    public MessageBus createMessageBus(String namespace) {
        String ns = namespace;

        if (isOpened()) {
            Log.e(TAG,
                    "cannot create MessageBus: FlintReceiverManager is already opened!");
            return null;
        }

        if (namespace == null) {
            ns = FlintConstants.DEFAULT_NAMESPACE;
        }

        if (mMessageChannel == null) {
            mMessageChannel = createMessageChannel(FlintConstants.DEFAULT_CHANNEL_NAME);
        }

        return createMessageBusInternal(ns);
    }

    /**
     * Set additional data.
     * 
     * @param data
     */
    public void setAdditionalData(String data) {
        Log.d(TAG, "set custom additionaldata: " + data);
        mCustAdditionalData = data;

        sendAdditionalData();
    }

    /**
     * Whether the receiver manager is already opened.
     * 
     * @return
     */
    private boolean isOpened() {
        if (mIpcChannel != null && mIpcChannel.isOpened()) {
            return true;
        }

        return false;
    }

    /**
     * Send IPC related message
     */
    private void ipcSend(String data) {
        // TODO
    }

    /**
     * Process received IPC message
     * 
     * @param data
     */
    private void onIpcMessage(JSONObject data) {
        try {
            String type = data.getString(IPC_MESSAGE_TYPE);

            if (type.equals(IPC_MESSAGE_STARTHEARTBEAT)) {
                Log.d(TAG, "receiver ready to start heartbeat!!!");
            } else if (type.equals(IPC_MESSAGE_REGISTEROK)) {
                Log.d(TAG, "receiver register done!!!");
            } else if (type.equals(IPC_MESSAGE_HEARTBEAT)) {
                String t = data.getString(IPC_MESSAGE_DATA_HEARTBEAT);
                if (t.equals(IPC_MESSAGE_DATA_HEARTBEAT_PING)) {
                    ipcSend("type: 'heartbeat', heartbeat: 'pong'");
                } else if (t.equals(IPC_MESSAGE_DATA_HEARTBEAT_PONG)) {
                    ipcSend("type: 'heartbeat', heartbeat: 'ping'");
                } else {
                    Log.e(TAG, "unknow heartbeat message:" + t);
                }
            } else if (type.equals(IPC_MESSAGE_SENDERCONNECTED)) {
                Log.d(TAG,
                        "IPC senderconnected: "
                                + data.getString(IPC_MESSAGE_DATA_TOKEN));
            } else if (type.equals(IPC_MESSAGE_SENDERDISCONNECTED)) {
                Log.d(TAG,
                        "IPC senderdisconnected: "
                                + data.getString(IPC_MESSAGE_DATA_TOKEN));
            } else {
                Log.e(TAG, "IPC unknow type:" + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send additional data
     */
    private void sendAdditionalData() {
        String additionalData = joinAdditionalData();
        if (additionalData != null) {
            ipcSend("type: 'additionaldata', additionaldata: " + additionalData);
        } else {
            Log.e(TAG, "no additionaldata need to send");
        }
    }

    /**
     * Get current additional data
     */
    private String joinAdditionalData() {
        // TODO

        return null;
    }

    /**
     * Create one MessageChannel object
     * 
     * @param channelName
     * @return
     */
    private MessageChannel createMessageChannel(String channelName) {
        String url = "ws://127.0.0.1:9439/channels/" + channelName;

        MessageChannel channel = new ReceiverMessageChannel(channelName, url) {
            @Override
            public void onOpen(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "Receiver default message channel open!!! " + data);
            }

            @Override
            public void onClose(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "Receiver default message channel close!!! " + data);
            }

            @Override
            public void onError(String data) {
                // TODO Auto-generated method stub

                Log.e(TAG, "Receiver default message channel error!!! " + data);
            }
        };

        return channel;
    }

    /**
     * Create MessageBus obj
     * 
     * @param namespace
     * @return
     */
    private MessageBus createMessageBusInternal(String namespace) {
        MessageBus messageBus = mMessageBusList.get(namespace);
        if (messageBus == null) {
            messageBus = new ReceiverMessageBus(mMessageChannel, namespace);
            mMessageBusList.put(namespace, messageBus);
        }

        return messageBus;
    }
}
