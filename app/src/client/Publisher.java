package client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Scanner;

public class Publisher {
    static final String ERROR = "ERROR";
    static final String OK = "OK";

    static String username;
    static String message;
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length <=0 || args[0] == null || args[1] == null){
            throw new InvalidParameterException("Usage : java Publisher address port");
        }

        Scanner scanner = new Scanner(System.in);
        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
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
        buffer.clear();
        buffer.flip();

        while(true) {
            Thread.sleep(1000);

            /*** receive message ***/
            client.read(buffer);
            String response = new String((buffer.array())).trim();
            System.out.println(response);

            /*** Close connexion ***/
            if (new String(buffer.array()).trim().equals(ERROR) || new String(buffer.array()).trim().equals(OK)) {
                buffer.clear();
                buffer.flip();
                message = "!QUIT";
                buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);
                System.out.println("Closing Connexion");
                break;
            }
        }
    }
}
