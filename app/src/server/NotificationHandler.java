package server;

import data.Database;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class NotificationHandler implements Runnable{

    SocketChannel channel;
    HashMap<String, SocketChannel> usernamesClients;

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(true){
            try {
                if(channel.read(buffer)>0) {
                    buffer.flip();
                    String response = new String(buffer.array(), buffer.position(), buffer.limit());
                    buffer.clear();
                    if (response.contains("NOTIFY")) {


                    }else{
                        System.out.println("Response from master : "+response);
                    }
                }
            } catch (IOException e) {
                System.out.println("Closing Stream");
                break;
            }
        }
    }

    public NotificationHandler(SocketChannel channel, HashMap<String, SocketChannel> usernamesClients) {
        this.channel = channel;
        this.usernamesClients = usernamesClients;
    }
}
