package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static client.Client.OK;

public class Subscriber implements ClientAction{

    String username;
    ByteBuffer buffer;
    SocketChannel client;

    public Subscriber(String username, ByteBuffer buffer, SocketChannel client) {
        this.username = username;
        this.buffer = buffer;
        this.client = client;
    }

    @Override
    public String getCommand() {
        String subscribingValue = getCleanInput();
        if(subscribingValue.contains("@"))
            return "SUBSCRIBE author:" + subscribingValue;
        if(subscribingValue.contains("#"))
            return "SUBSCRIBE tag:" + subscribingValue;
        return null;
    }

    @Override
    public void run() throws IOException, InterruptedException {
        while(true){
            String message = this.getCommand();
            buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);
            buffer.clear();

            /*** receive message ***/
            client.read(buffer);
            String response = new String(buffer.array(), 0, buffer.position());
            buffer.flip();
            buffer.clear();

            System.out.println(response);

            /*** Close connexion ***/
            if (response.contains(OK)) {
                break;
            }
        }
    }

    public String getCleanInput(){
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
