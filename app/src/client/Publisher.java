package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static client.Client.ERROR;
import static client.Client.OK;

public class Publisher implements ClientAction {
    String username;
    public Publisher(String username) {
        this.username = username;
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
        InetAddress address = InetAddress.getByName("localhost");
        int port = 12345;
        SocketChannel client = SocketChannel.open(new InetSocketAddress(address, port));
        ByteBuffer buffer;
        client.configureBlocking(true);
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
                message = "!QUIT";
                buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);
                System.out.println("Closing Connexion");
                break;
            }
        }

    }

}
