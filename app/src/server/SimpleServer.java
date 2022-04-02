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


    public void handleRepublish(SocketChannel client, String result) throws IOException {
        ByteBuffer buffer;
        HashMap<String, String> parserRepublish = Parser.parseRepublish(result);
        if (db.getUserDB().isUsernameRegistered(parserRepublish.get("author")) && db.getIdMessage().containsKey(Long.parseLong(parserRepublish.get("id")))) {
            buffer = ByteBuffer.wrap(db.getIdMessage().get(Long.parseLong(parserRepublish.get("id"))).getCore().getBytes());
            client.write(buffer);
        }

        buffer = ByteBuffer.wrap("OK\r\n".getBytes());
        client.write(buffer);
    }


    public void handleReply(SocketChannel client, String result) throws IOException {
        ByteBuffer buffer;
        HashMap<String, String> command_reply = Parser.parseReply(result);

        long id_reply = generateID();
        User user_reply = db.getUserDB().getUserByUsername(command_reply.get("author"));
        Message message_reply = new Message(command_reply.get("core"), id_reply, user_reply);

        db.getMessages().add(message_reply);
        db.getIdMessage().put(id_reply, message_reply);


        /*** verification of the message being replied to ***/
        if (db.getIdMessage().containsKey(Long.parseLong(command_reply.get("id")))) {
            db.getUserDB().addMessage(user_reply, id_reply);
            buffer = ByteBuffer.wrap(command_reply.get("core").getBytes());
            client.write(buffer);
            buffer.clear();
            buffer.flip();
        }

        buffer = ByteBuffer.wrap("OK\r\n".getBytes());
        client.write(buffer);
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

    public void handleSubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
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


    public void handleRCVMSG(SocketChannel client, String result) throws IOException {
        ByteBuffer buffer;
        HashMap<String, String> command = Parser.parseRCVMSG(result);
        long idMsg = Long.parseLong(command.get("Msg_id"));
        if (!db.getIdMessage().containsKey(idMsg)) {
            sendERROR(client, "Unknown message id : " + idMsg + "\r\n");
        } else {
            //list_id_message.put(id_msg,message.getCore());
            System.out.println(responseMSG(idMsg));
            buffer = ByteBuffer.wrap(responseMSG(idMsg).getBytes());
            client.write(buffer);
        }
    }

    public void handleRCVIDS(SocketChannel client, String result) throws IOException {
        ByteBuffer buffer;
        HashMap<String, String> command = Parser.parseRCVIDS(result);
        buffer = ByteBuffer.wrap(responseMSGIDS(
                        command.get("author"),
                        command.get("tag"),
                        command.get("sinceID") == null ? 0 : Long.parseLong(command.get("sinceId")),
                        command.get("limit") == null ? 5 : Integer.parseInt(command.get("limit"))
                ).getBytes()
        );
        client.write(buffer);
    }

    public void handlePublish(SocketChannel client, String result) throws IOException {
        HashMap<String, String> command = Parser.parsePublish(result);
        User author = new User(command.get("author"));
        long id = generateID();
        System.out.println("Message id: " + id + " from " + command.get("author"));
        Message message = new Message(command.get("core"), id, author);
        if (message.getTags() != null) db.getTags().addAll(message.getTags());

        db.getUserDB().addMessage(author, id);

        db.getIdMessage().put(id, message);

        System.out.println(message.getCore());
        sendOK(client);

        notifyFollowers(db.getUserDB().getUserByUsername(command.get("author")), message);
    }

    @Override
    public void notifyFollowers(User author, Message message) {
        List<String> followersUsernames = db.getUserDB().getFollowersUsernames(author);
        List<String> tagFollowersUsernames = new ArrayList<>();
        for (String tag : message.getTags()) {
            tagFollowersUsernames.addAll(db.getUserDB().getTagFollowersUsernames(tag));
        }
        for (String username : followersUsernames) {
            SocketChannel client = db.getUsernamesClient().get(username);
            ByteBuffer buffer = ByteBuffer.wrap(responseMSG(message.getId()).getBytes());
            try {
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.clear();
        }
        for (String username : tagFollowersUsernames) {
            if (followersUsernames.contains(username)) continue;
            SocketChannel client = db.getUsernamesClient().get(username);
            ByteBuffer buffer = ByteBuffer.wrap(responseMSG(message.getId()).getBytes());
            try {
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.clear();
        }
    }


}

