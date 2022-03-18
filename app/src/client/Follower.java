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

    public Follower(String username) {
        this.username = username;
    }

    public void run() throws IOException, InterruptedException {
        InetAddress address = InetAddress.getByName("localhost");
        int port = 12345;
        SocketChannel client = SocketChannel.open(new InetSocketAddress(address, port));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.configureBlocking(true);

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
                String message = "!QUIT";
                buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);
                System.out.println("Closing Connexion");
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
