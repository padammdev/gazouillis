package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

public class StreamHandler implements Runnable{

    SocketChannel channel;
    ByteBuffer buffer;
    ArrayBlockingQueue<String> queue;

    @Override
    public void run() {
        while(true){
            try {
                if(channel.read(buffer)>0) {
                    buffer.flip();
                    String response = new String(buffer.array(), buffer.position(), buffer.limit());
                    buffer.clear();
                    if (response.contains("MSG")) {
                        System.out.println("New Notification !\r\n" + response);
                    }else{
                        System.out.println("Response from server : "+response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public StreamHandler(SocketChannel channel,ArrayBlockingQueue<String> queue){
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(1024);
        this.queue = queue;
    }
}
