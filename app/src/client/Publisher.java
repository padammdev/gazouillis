package client;

import java.util.Scanner;

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

}
