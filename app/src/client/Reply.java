package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLOutput;
import java.util.Scanner;

import static client.Client.OK;

public class Reply implements ClientAction{
    String username;
    ByteBuffer buffer;
    SocketChannel client;

    public Reply(String username, ByteBuffer buffer, SocketChannel client) {
        this.username = username;
        this.buffer = buffer;
        this.client = client;
    }

    @Override
    public String getCommand() {
        System.out.println("You want to reply to which message ? (we need the id)");
        Scanner scanner = new Scanner(System.in);
        long id = scanner.nextLong();

        System.out.println("Enter the reply: ");
        Scanner scanner1 = new Scanner(System.in);
        String reply = scanner1.nextLine();

        return "REPLY author:" + username + " To_msg_id: " + id + "\n" + reply;
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
}
