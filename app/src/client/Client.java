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

        System.out.print("Enter a username: ");
        username = scanner.nextLine();

        while (username.contains(" ")) {
            System.out.println("Username must contain only alphanumeric caracters without spaces ");
            System.out.print("Enter a username: ");
            username = scanner.nextLine();
        }
        username = "@" + username;
        System.out.println("Hi" + username + " !");

        int choice = getAction();


        switch (choice) {
            case 1:
                new Publisher(username).run();
                break;
            case 2:
                new Follower(username).run();
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }


    }

    public static int getAction() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(
                "What do you want to do:\n" +
                "1. Publish a message\n" +
                "2. Receive 5 last messages of specified user(s)\n" +
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
