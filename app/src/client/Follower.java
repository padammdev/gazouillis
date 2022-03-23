package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
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
            System.out.println(command);
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
        String[] followedKeys = cleanInput();
        System.out.println(Arrays.deepToString(followedKeys));
        if(containsOnlyAt(followedKeys)) return requestWithAuthors(followedKeys);
        else if (containsOnlyTag(followedKeys)) return requestWithTags(followedKeys);
        else return requestWithAuthorTags(followedKeys);


    }

    private String requestWithAuthors(String[] params){
        StringBuilder command = new StringBuilder();
        for (String key: params) {
            command.append("RCV_IDS author:").append(key).append("\r\n");

        }

        return command.toString();
    }

    private String requestWithTags(String[] params){
        StringBuilder command = new StringBuilder();
        for (String key: params) {
            command.append("RCV_IDS tag:").append(key).append("\r\n");

        }

        return command.toString();
    }

    private String requestWithAuthorTags(String[] params){
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        StringBuilder command = new StringBuilder();
        for (String key: params) {
           if(key.charAt(0) == '@') authors.add(key);
           else if(key.charAt(0) == '#') tags.add(key);
        }
        for(String tag : tags){
            for(String author : authors){
                command.append("RCV_IDS ").append("author:").append(author).append(" ").append("tag:").append(tag).append("\r\n");
            }
        }
        return command.toString();
    }

    private String[] cleanInput(){
        Scanner scanner = new Scanner(System.in);
        String[] followedKeys;
        boolean isInputClean;
        do {
            System.out.print("Enter username(s)/tag(s) of the user(s)/tag(s) you want to receive separated with comas (Specify user AND tag to combine reception): ");
            String input = scanner.nextLine();
            isInputClean = true;
            if (input.contains(",")){
                followedKeys = input.split(",");
            }
            else followedKeys = new String[]{input};
            System.out.println(Arrays.deepToString(followedKeys));
            for (String key : followedKeys) {
                if (key.charAt(0) != '@' && key.charAt(0) != '#') {
                    System.out.println("Usernames must begin with a @ and Tags with #");
                    isInputClean = false;
                }
                if (key.contains(" ")) {
                    System.out.println("Usernames and Tags must not contains spaces");
                    isInputClean = false;
                }
            }


        } while (!isInputClean);
        return followedKeys;
    }

    private boolean containsOnlyAt(String[] keys){
        for(String key : keys){
            if(key.charAt(0) != '@') return false;
        }
        return true;
    }

    private boolean containsOnlyTag(String[] keys){
        for(String key : keys) {
            if (key.charAt(0) != '#') return false;
        }
        return true;
    }

}
