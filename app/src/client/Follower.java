package client;

import java.util.Scanner;

public class Follower implements ClientAction {

    String username;

    public Follower(String username) {
        this.username = username;
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
