package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public abstract class StreamClient {

    String username;
    ByteBuffer buffer;
    SocketChannel client;

    public StreamClient(String username, ByteBuffer buffer, SocketChannel client) {
        this.username = username;
        this.buffer = buffer;
        this.client = client;
    }

    abstract String getCommand();
    void run() throws IOException, InterruptedException {
        String message = this.getCommand();
        buffer = ByteBuffer.wrap(message.getBytes());
        client.write(buffer);
        buffer.clear();

    }

    String getCleanInput(){
        Scanner scanner = new Scanner(System.in);
        boolean isInputClean;
        String subscribingValue;
        do{
            isInputClean = true;
            System.out.print("Enter the username or the tag you want to subscribe to : ");
            subscribingValue = scanner.nextLine();
            if(subscribingValue.charAt(0) != '@' && subscribingValue.charAt(0) != '#'){
                System.out.println("Usernames must start with @ and tags with #");
                isInputClean = false;
            }
            if(subscribingValue.contains(" ")){
                System.out.println("Usernames and Tags must not contain spaces");
                isInputClean = false;
            }
        }while(!isInputClean);
        return subscribingValue;
    }

}
