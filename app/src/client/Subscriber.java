package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static client.Client.OK;

public class Subscriber extends StreamClient {

    String username;
    ByteBuffer buffer;
    SocketChannel client;

    public Subscriber(String username, ByteBuffer buffer, SocketChannel client) {
        super(username, buffer, client);
    }

    public String getCommand() {
        String subscribingValue = getCleanInput();
        if(subscribingValue.contains("@"))
            return "SUBSCRIBE user:" + subscribingValue;
        if(subscribingValue.contains("#"))
            return "SUBSCRIBE tag:" + subscribingValue;
        return null;
    }



}
