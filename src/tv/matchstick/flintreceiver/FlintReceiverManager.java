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
    private static final String IPC_MESSAGE_APPID = "appid";

    private static final String IPC_MESSAGE_DATA = "data";

    private static final String IPC_MESSAGE_DATA_TOKEN = "token";
    private static final String IPC_MESSAGE_DATA_HEARTBEAT = "heartbeat";
    private static final String IPC_MESSAGE_DATA_HEARTBEAT_PING = "ping";
    private static final String IPC_MESSAGE_DATA_HEARTBEAT_PONG = "pong";

    private static final String IPC_MESSAGE_DATA_CHANNELBASEURL = "channelBaseUrl";
    private static final String IPC_MESSAGE_DATA_SERVICE_INFO = "service_info";
    private static final String IPC_MESSAGE_DATA_SERVICE_INFO_IP = "ip";
    private static final String IPC_MESSAGE_DATA_ADDITIONALDATA = "additionaldata";
    private static final String IPC_MESSAGE_DATA_REGISTER = "register";
    private static final String IPC_MESSAGE_DATA_UNREGISTER = "unregister";

    private static final String IPC_MESSAGE_TYPE_HEARTBEAT = "heartbeat";
    private static final String IPC_MESSAGE_TYPE_ADDITIONALDATA = "additionaldata";

    private String mAppId;

    private MessageChannel mIpcChannel;

    private MessageChannel mMessageChannel;

    private String mIpcAddress;

    private String mFlintServerIp = "127.0.0.1";

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
                JSONObject register = new JSONObject();
                try {
                    register.put(IPC_MESSAGE_TYPE, IPC_MESSAGE_DATA_REGISTER);
                    ipcSend(register);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    JSONObject json = new JSONObject(data);

                    onIpcMessage(json);
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
            JSONObject unregister = new JSONObject();
            try {
                unregister.put(IPC_MESSAGE_TYPE, IPC_MESSAGE_DATA_UNREGISTER);
                ipcSend(unregister);
            } catch (Exception e) {
                e.printStackTrace();
            }

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

        MessageBus messageBus = mMessageBusList.get(ns);
        if (messageBus == null) {
            messageBus = new ReceiverMessageBus(mMessageChannel, ns);
            mMessageBusList.put(ns, messageBus);
        }

        return messageBus;
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
    private void ipcSend(JSONObject data) {
        JSONObject json = data;
        try {
            json.put(IPC_MESSAGE_APPID, mAppId);

            if (mIpcChannel != null) {
                mIpcChannel.send(data.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                mFlintServerIp = data
                        .getJSONObject(IPC_MESSAGE_DATA_SERVICE_INFO)
                        .getJSONArray(IPC_MESSAGE_DATA_SERVICE_INFO_IP)
                        .getString(0);

                sendAdditionalData();
            } else if (type.equals(IPC_MESSAGE_HEARTBEAT)) {
                String t = data.getString(IPC_MESSAGE_DATA_HEARTBEAT);
                if (t.equals(IPC_MESSAGE_DATA_HEARTBEAT_PING)) {
                    JSONObject pong = new JSONObject();
                    try {
                        pong.put(IPC_MESSAGE_TYPE, IPC_MESSAGE_DATA_HEARTBEAT);
                        pong.put(IPC_MESSAGE_TYPE_HEARTBEAT,
                                IPC_MESSAGE_DATA_HEARTBEAT_PONG);
                        ipcSend(pong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (t.equals(IPC_MESSAGE_DATA_HEARTBEAT_PONG)) {
                    JSONObject pong = new JSONObject();
                    try {
                        pong.put(IPC_MESSAGE_TYPE, IPC_MESSAGE_DATA_HEARTBEAT);
                        pong.put(IPC_MESSAGE_TYPE_HEARTBEAT,
                                IPC_MESSAGE_DATA_HEARTBEAT_PING);
                        ipcSend(pong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        JSONObject additionalData = joinAdditionalData();

        if (additionalData != null) {
            JSONObject data = new JSONObject();
            try {
                data.put(IPC_MESSAGE_TYPE, IPC_MESSAGE_DATA_ADDITIONALDATA);
                data.put(IPC_MESSAGE_TYPE_ADDITIONALDATA, additionalData);
                ipcSend(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "no additionaldata need to send");
        }
    }

    /**
     * Get current additional data
     */
    private JSONObject joinAdditionalData() {
        JSONObject additionalData = new JSONObject();

        try {
            if (mMessageChannel != null) {
                additionalData.put(IPC_MESSAGE_DATA_CHANNELBASEURL,
                        "ws://" + mFlintServerIp + ":9439/channels/"
                                + mMessageChannel.getName());
            }

            if (mCustAdditionalData != null) {
                additionalData.put("customData", mCustAdditionalData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (additionalData.length() > 0) {
            return additionalData;
        }

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
}
