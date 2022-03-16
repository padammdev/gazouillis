package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    static final String ERROR = "ERROR";
    static final String OK = "OK";
    static final String MSG_IDS = "MSG_IDS";

    static String username;
    static String message;

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        InetAddress address = InetAddress.getByName("localhost");
        int port = 12345;
        SocketChannel client = SocketChannel.open(new InetSocketAddress(address, port));
        ByteBuffer buffer = null;
        client.configureBlocking(true);

        /*** send message ***/
        System.out.print("Enter a username: ");
        username = scanner.nextLine();

        while (username.contains(" ")) {
            System.out.println("Username must contain only alphanumeric caracters without spaces ");
            System.out.print("Enter a username: ");
            username = scanner.nextLine();
        }
        username = "@" + username;
        System.out.println("Hi" + username+ " !");

        int choice = getAction();

        while (true) {
            switch (choice) {
                case 1:
                    String message = new Publisher(username).getCommand();
                    buffer = ByteBuffer.wrap(message.getBytes());
                    client.write(buffer);
                    buffer.clear();
                    break;
                case 2:
                    String command = new Follower(username).getCommand();
                    for(String request : command.split("\r\n")){
                        buffer = ByteBuffer.wrap(request.getBytes());
                        client.write(buffer);
                        buffer.clear();
                        Thread.sleep(500);
                        client.read(buffer);
                        String response = new String(buffer.array(), 0, buffer.position());
                        buffer.flip();
                        buffer.clear();
                        System.out.println(response);
                        /*** Handle errors ***/
                        if (response.contains(ERROR)){

                            choice = getAction();
                        }

                        /*** Handle MSG_IDS Response ***/
                        if(response.contains(MSG_IDS)){
                            String[] parsedResponse = response.split("\r\n");
                            System.out.println(Arrays.toString(parsedResponse));
                            for(int i = 1; i<parsedResponse.length; i++){
                                String rcvRequest = "RCV_MSG msg_id:"+parsedResponse[i];
                                buffer = ByteBuffer.wrap(rcvRequest.getBytes());
                                client.write(buffer);
                                buffer.clear();
                                Thread.sleep(500);
                                client.read(buffer);
                                String msgResponse = new String(buffer.array(), 0, buffer.position());
                                buffer.flip();
                                buffer.clear();
                                System.out.println(msgResponse);
                                /*** Handle errors ***/
                                if (msgResponse.contains(ERROR)){
                                    choice = getAction();
                                }
                            }
                        }

                    }
                    message = "!QUIT";
                    buffer = ByteBuffer.wrap(message.getBytes());
                    client.write(buffer);
                    System.out.println("Closing Connexion");

                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
            }

            /*** receive message ***/
            client.read(buffer);
            String response = new String(buffer.array(), 0, buffer.position());
            buffer.flip();
            buffer.clear();

            /*** Handle errors ***/
            if (response.contains(ERROR)){
                System.out.println(response);
                choice = getAction();
            }

            /*** Close connexion ***/
            if (choice == 1 && response.contains(OK)) {
                message = "!QUIT";
                buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);
                System.out.println("Closing Connexion");
                break;
            }

        }
    }
    public static int getAction(){
        Scanner scanner = new Scanner(System.in);
        System.out.println(
                "What do you want to do:\n" +
                "1. Publish a message\n" +
                "2. Receive 5 last messages of specified user(s)\n"+
                "4. Subscribe to a user\n" +
                "5. Unsubscribe to a user\n");
        String input;
        int choice = 1;
        do {
            input = scanner.nextLine();
            if (!Character.isDigit(input.charAt(0))) {
                System.out.println("Your answer must be a digit corresponding to the action you wanted to do");
            } else choice = Integer.parseInt(String.valueOf(input.charAt(0)));
        } while (!Character.isDigit(input.charAt(0)));
        return choice;
    }

}
