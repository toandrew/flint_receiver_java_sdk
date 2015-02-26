package tv.matchstick.flintreceiver;

import java.util.ArrayList;

/**
 * This is used to transfer messages between sender and receiver Apps.
 * 
 * @author jim
 *
 */
public class ReceiverMessageBus extends MessageBus {

    ReceiverMessageBus(MessageChannel channel, String namespace) {
        super(channel, namespace);
        // TODO Auto-generated constructor stub

        init();
    }

    @Override
    public void send(String data, String senderId) {
        // TODO Auto-generated method stub

    }

    @Override
    public ArrayList<String> getSenders() {
        // TODO Auto-generated method stub
        return null;
    }

    private void init() {

    }

}
