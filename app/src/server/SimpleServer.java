package server;


import data.Message;
import data.User;
import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.channels.*;

public class SimpleServer extends Server implements RequestHandler {

    @Override
    public void init() throws IOException {
        selector = Selector.open();
        ssc = ServerSocketChannel.open();
        port = 12345;
        localhost = new InetSocketAddress("localhost", port );
        ssc.bind(localhost);
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server ok");
    }


    public void handleUnsubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        HashMap<String, String> command = Parser.parseSubscribe(result);
        if (command.containsKey("user")) {
            if (!db.getUserDB().isUsernameRegistered(command.get("user")))
                buffer = ByteBuffer.wrap((ERROR + "User does not exist").getBytes(StandardCharsets.UTF_8));
            else {
                db.getUserDB().computeUserUnfollow(command.get("user"), (String) key.attachment());
                buffer = ByteBuffer.wrap((OK).getBytes());
            }
        } else if (command.containsKey("tag")) {
            if (!db.getTags().contains(command.get("tag"))) db.getTags().add(command.get("tag"));
            db.getUserDB().computeTagUnfollow(command.get("tag"), db.getUserDB().getUserByUsername((String) key.attachment()));
            buffer = ByteBuffer.wrap((OK).getBytes());
        }
        client.write(buffer);
    }

    @Override
    public void handleServerConnect(SocketChannel peer) throws IOException {

    }

    @Override
    public void handlePeerRequestUserConnect(SocketChannel peer, String result, SelectionKey key) throws IOException {

    }

    @Override
    public void handlePeerRequestSubscribe(SocketChannel peer, String result) throws IOException {

    }

    @Override
    public void handlePeerRequestUnsubscribe(SocketChannel peer, String result) throws IOException {

    }

    @Override
    public void handleNotificationRequest(String result) {

    }

    public void handleSubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {
        HashMap<String, String> command = Parser.parseSubscribe(result);
        if (command.containsKey("user")) {
            if (!db.getUserDB().isUsernameRegistered(command.get("user")))
                sendERROR(client, "Username does not exist\r\n");
            else {
                db.getUserDB().computeUserFollow(command.get("user"), (String) key.attachment());
                sendOK(client);
            }
        } else if (command.containsKey("tag")) {
            if (!db.getTags().contains(command.get("tag"))) db.getTags().add(command.get("tag"));
            db.getUserDB().computeTagFollow(command.get("tag"), db.getUserDB().getUserByUsername((String) key.attachment()));
            sendOK(client);
        }
    }






}

