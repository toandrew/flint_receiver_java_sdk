package tv.matchstick.flintreceiver;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.util.Log;

public class FlintWebSocket extends WebSocketClient {
    private static final String TAG = "FlintWebSocket";

    private final FlintWebSocketListener mSocketListener;

    public FlintWebSocket(FlintWebSocketListener listener, URI serverURI) {
        super(serverURI);

        Log.d(TAG, "url = " + serverURI.toString());

        mSocketListener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "open");

        mSocketListener.onOpen("");
    }

    @Override
    public void onMessage(String message) {
        mSocketListener.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "close: " + code + "; " + reason);

        mSocketListener.onClose(reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.d(TAG, "error");

        mSocketListener.onError(ex.toString());
    }
}
