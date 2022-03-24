package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static client.Client.OK;

public class Publisher extends RequestClient {

    public Publisher(String username) {
        super(username);
    }

    @Override
    public String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your message :");
        String message = scanner.nextLine();
        if (message.length() > 256) {
            System.out.println("The message is too long. It has been truncated");
            message = message.substring(0, 256);
        }
        return "PUBLISH author:" + username + "\r\n" + message + "\r\n";
    }

    @Override
    public void run() throws IOException {
        init();
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
                closeConnection();
                break;
            }
        }

    }

}
