package tv.matchstick.flintreceiver;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Flint WebSocket
 * 
 * @author jim
 *
 */
public class FlintWebSocket extends WebSocketClient {
    private static final String TAG = "FlintWebSocket";

    private FlintLogger log = new FlintLogger(TAG);

    private final FlintWebSocketListener mSocketListener;

    public FlintWebSocket(FlintWebSocketListener listener, URI serverURI) {
        super(serverURI);

        log.d("url = " + serverURI.toString());

        mSocketListener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.d("open");

        mSocketListener.onOpen("");
    }

    @Override
    public void onMessage(String message) {
        mSocketListener.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.e("close: " + code + "; " + reason);

        mSocketListener.onClose(reason);
    }

    @Override
    public void onError(Exception ex) {
        log.e("error");

        mSocketListener.onError(ex.toString());
    }
}
