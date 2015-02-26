package tv.matchstick.flintreceiver;

import java.util.ArrayList;

/**
 * Receiver's message channel.
 * 
 * @author jim
 *
 */
public class ReceiverMessageChannel extends MessageChannel {

    public ReceiverMessageChannel(String name, String url) {
        super(name, url);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void send(String data) {
        super.send(data);
    }

    @Override
    public ArrayList<String> getSenders() {
        // TODO Auto-generated method stub
        return null;
    }

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
