package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Scanner;

import static client.Client.ERROR;
import static client.Client.MSG_IDS;

public class Follower implements ClientAction {

    String username;
    ByteBuffer buffer;
    SocketChannel client;

    public Follower(String username, ByteBuffer buffer, SocketChannel client) {
        this.username = username;
        this.buffer = buffer;
        this.client = client;
    }

    public void run() throws IOException, InterruptedException {

        while (true) {
            boolean hasErrors = false;
            String command = this.getCommand();
            for (String request : command.split("\r\n")) {

                buffer.put(request.getBytes());
                buffer.flip();
                client.write(buffer);

                buffer.clear();
                //Thread.sleep(500);

                client.read(buffer);
                buffer.flip();
                String response = new String(buffer.array(), buffer.position(), buffer.limit());
                buffer.clear();
                System.out.println(response);
                /*** Handle errors ***/
                if (response.contains(ERROR)) {
                    hasErrors = true;
                    break;
                }

                /*** Handle MSG_IDS Response ***/
                if (response.contains(MSG_IDS)) {
                    String[] parsedResponse = response.split("\r\n");
                    for (int i = 1; i < parsedResponse.length; i++) {
                        String rcvRequest = "RCV_MSG msg_id:" + parsedResponse[i];
                        buffer.put(rcvRequest.getBytes());
                        buffer.flip();
                        client.write(buffer);
                        buffer.clear();


                        client.read(buffer);
                        buffer.flip();
                        String msgResponse = new String(buffer.array(), buffer.position(), buffer.limit());

                        buffer.clear();
                        System.out.println(msgResponse);
                        /*** Handle errors ***/
                        if (msgResponse.contains(ERROR)) {
                            hasErrors = true;
                            break;
                        }
                    }
                }

            }
            if (!hasErrors) {
                break;
            }
        }

    }

    @Override
    public String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username of the users you want to subscribe to (separated with comas): ");
        String message;
        String[] usernames;
        boolean isInputClean;
        do {
            isInputClean = true;
            message = scanner.nextLine();
            if (message.contains(",")) usernames = message.split(",");
            else usernames = new String[]{message};
            for (String username : usernames) {
                if (username.charAt(0) != '@') {
                    System.out.println("Usernames must begin with a @");
                    isInputClean = false;
                }
                if (username.contains(" ")) {
                    System.out.println("Usernames must not contains spaces");
                    isInputClean = false;
                }
            }


        } while (!isInputClean);

        StringBuilder command = new StringBuilder();
        for (String username : usernames) {
            command.append("RCV_IDS author:").append(username).append("\r\n");
        }

        return command.toString();
    }


}
