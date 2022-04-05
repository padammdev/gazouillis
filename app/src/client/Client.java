package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

public class Client {
    static final String ERROR = "ERROR";
    static final String OK = "OK";
    static final String MSG_IDS = "MSG_IDS";

    static String username;
    static String message;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Which port you want to connect ?");
        Scanner scannerPort = new Scanner(System.in);
        int port = scannerPort.nextInt();
        InetAddress address = InetAddress.getByName("localhost");
        //int port = 12345;
        SocketChannel client = SocketChannel.open(new InetSocketAddress(address, port));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.configureBlocking(true);

        ArrayBlockingQueue<String> stream = new ArrayBlockingQueue<>(5);

        boolean isUsernameOK = false;
        do {
            username = getUsername();
            String register = "CONNECT username:" + username + "\r\n";
            buffer.put(register.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            buffer.flip();
            String response = new String(buffer.array(), buffer.position(), buffer.limit());
            buffer.clear();
            System.out.println(response);
            if (response.contains("OK")) isUsernameOK = true;
        } while (!isUsernameOK);
        System.out.println("Hi " + username + " !");
        Thread worker = new Thread(new StreamHandler(client, stream));
        worker.start();
        worker.setPriority(Thread.MIN_PRIORITY);
        int choice;

        do {
            choice = getAction();
            switch (choice) {
                case 1:
                    new Publisher(username, port).run();
                    break;
                case 2:
                    new Follower(username, port).run();
                    break;
                case 3:
                    new Republish(username, port).run();
                    break;
                case 4:
                    new Reply(username, port).run();
                    break;
                case 5:
                    new Subscriber(username, buffer, client).run();
                    break;
                case 6:
                    new Unsubscriber(username, buffer, client).run();
                    break;
                case 7:
                    String message = "!QUIT";
                    buffer = ByteBuffer.wrap(message.getBytes());
                    client.write(buffer);
                    System.out.println("Closing Connexion");
                    break;
            }
        } while (choice != 7);
        worker.interrupt();

    }

    public static int getAction() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(
                "What do you want to do:\n" +
                "1. Publish a message\n" +
                "2. Receive most recent messages of specified user(s) and/or tag(s)\n" +
                "3. Republish a message\n" +
                "4. Reply to a message\n" +
                "5. Subscribe to a user or a tag\n" +
                "6. Unsubscribe to a user or a tag\n" +
                "7. Quit \n");
        return getResponse(scanner);
    }

    public static String getUsername() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a username (starting with @): ");
        String username = scanner.nextLine();

        while (username.contains(" ") || !username.contains("@")) {
            System.out.println("Username must contain only alphanumeric caracters without spaces and must start with @");
            System.out.print("Enter a username: ");
            username = scanner.nextLine();
        }
        return username;
    }

    static int getResponse(Scanner scanner) {
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
