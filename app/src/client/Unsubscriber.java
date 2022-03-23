package client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Unsubscriber extends Subscriber{

    public Unsubscriber(String username, ByteBuffer buffer, SocketChannel client) {
        super(username, buffer, client);
    }

    @Override
    public String getCommand() {
        String unsubscribingValue = super.getCleanInput();
        if(unsubscribingValue.contains("@"))
            return "UNSUBSCRIBE author:" + unsubscribingValue;
        if(unsubscribingValue.contains("#"))
            return "UNSUBSCRIBE tag:" + unsubscribingValue;
        return null;
    }
}