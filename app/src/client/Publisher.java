package client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Publisher {
    static final String ERROR = "ERROR";
    static final String OK = "OK\r\n";

    static String username;
    static String message;
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        InetAddress address = InetAddress.getByName("localhost");
        int port = 12345;
        SocketChannel client =  SocketChannel.open(new InetSocketAddress(address, port));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.configureBlocking(true);

        /*** send message ***/
        System.out.print("Enter a username: ");
        username = scanner.nextLine();

        while(username.contains(" ")){
            System.out.println("Username must contain only alphanumeric caracters without spaces ");
            System.out.print("Enter a username: ");
            username = scanner.nextLine();
        }

        username = "@"+username;
        System.out.println("Enter your message :");
        message = scanner.nextLine();
        if(message.length()>256) {
            System.out.println("The message is too long. It has been truncated");
            message = message.substring(0,256);
        }

        /*** send message ***/
        message= "PUBLISH author:"+username+"\r\n"+message+"\r\n";
        buffer = ByteBuffer.wrap(message.getBytes());
        client.write(buffer);
        buffer.flip();
        buffer.clear();
        System.out.println(new String((buffer.array())).trim());
        while(true) {
            Thread.sleep(1000);
            /*** receive message ***/
            client.read(buffer);
            String response = new String((buffer.array())).trim();
            System.out.println(response);

            /*** Close connexion ***/
            if (response.equals(ERROR) || response.equals(OK)) {
                buffer.flip();
                buffer.clear();
                message = "!QUIT";
                buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);
                System.out.println("Closing Connexion");
                break;
            }
        }
    }
}
