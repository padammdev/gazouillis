package server;

import data.Message;
import data.User;

import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.nio.channels.*;

public class SimpleServer {

    static ArrayList<Long> msg_ids = new ArrayList<>();
    static HashMap<Long,String> list_id_message = new HashMap<>();
    static final String POISON_PILL = "!QUIT";
    static ArrayList<Message> messages = new ArrayList<>();
    static HashMap<User, List<Message>> user_messages = new HashMap<>();
    //static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Object> users = new ArrayList<>();
    static HashMap<User, List<User> > list_of_followers = new HashMap<>();

    static String ERROR = "ERROR : ";
    static String OK = "OK\r\n";

    public static void main(String[] arg) throws IOException, IOException {
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress localhost = new InetSocketAddress("localhost", 12345);
        ssc.bind(localhost);
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server ok");


        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    SocketChannel client = ssc.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Client Connected");

                    users.add(key.attachment());
                    System.out.println("Number of users : " + users.size());

                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    if (client.read(buffer) <= 0) key.cancel();
                    else {
                        buffer.flip();
                        String result = new String((buffer.array())).trim(); // trim() retuourne une copie du message +
                        String type = Parser.getCommandType(result);

                        switch (type) {

                            /*** PUBLISH ***/

                            case "PUBLISH":

                                HashMap<String, String> command = Parser.parsePublish(result);
                                User author = new User(command.get("author"));
                                long id = generateID();
                                System.out.println("Message id: " + id + " from " + command.get("author"));
                                Message message = new Message(command.get("core"), id, author);

                                /*** add messages for each user ***/
                                if(!users.contains(author)){
                                    users.add(author);
                                    user_messages.computeIfAbsent(author, m -> new ArrayList<>()).add(message);
                                    System.out.println(user_messages.toString());
                                }

                                /*** add id for each message ***/
                                list_id_message.put(message.getId(),message.getCore());
                                //list_id_message.computeIfAbsent(id, m -> message.getCore());
                                System.out.println(list_id_message);

                                //messages.add(message);
                                System.out.println(message.getCore());
                                buffer = ByteBuffer.wrap("OK\r\n\r\n".getBytes());
                                client.write(buffer);
                                System.out.println(new String((buffer.array())).trim());
                                break;

                            /*** RCV_IDS ***/

                            case "RCV_IDS":
                                command = Parser.parseRCVIDS(result);
                                buffer = ByteBuffer.wrap(responseMSGIDS(
                                        command.get("author"),
                                        command.get("tag"),
                                        command.get("sinceID") == null ? 0 : Long.parseLong(command.get("sinceId")),
                                        command.get("limit") == null ? 5 : Integer.parseInt(command.get("limit"))
                                        ).getBytes()
                                );
                                client.write(buffer);
                                break;

                            case "RCV_MSG":
                                command = Parser.parseRCVMSG(result);
                                long id_msg = Long.parseLong(command.get("Msg_id"));
                                if (!msg_ids.contains(id_msg)) {
                                    buffer = ByteBuffer.wrap((ERROR + "Unknown message id\r\n").getBytes());
                                } else {
                                    //list_id_message.put(id_msg,message.getCore());
                                    buffer = ByteBuffer.wrap(responseMSG(id_msg).getBytes());
                                }
                                client.write(buffer);

                                break;
                            case "FOLLOW" :
                                /* ex : FOLLOW User1 UserFollowed
                                command = Parser.parseFollow(result);
                                User user = Parser.getFirstUser();
                                User user2 = Parser.getUserfollowed();
                                list_of_followers.put(user, m -> new ArrayList<>()).add(user2);
                                System.out.println(user.getUsername() + " follow " + user2.getUsername());
                                */
                                break;

                            default:
                                buffer = ByteBuffer.wrap((ERROR + "Unknown command\r\n").getBytes());
                                client.write(buffer);
                                break;
                        }

                        buffer.clear();
                        if (new String(buffer.array()).trim().equals(POISON_PILL)) {
                            client.close();
                            System.out.println("Closing connexion");
                        }

                        if (key.attachment() == null) {
                            key.attach(1);
                        } else {
                            key.attach(Integer.parseInt(key.attachment().toString()) + 1);
                        }
                    }
                }

                iterator.remove();
            }

        }


    }
    /*
    public static String send_message_from_user(User user){
        String messages;
        for (int i=0 ; i< )
    }
    */
    public static HashMap<User,List<Message>> list_of_messages(Message message){
        if(!users.contains(message.getAuthor())){
            users.add(message.getAuthor());
            user_messages.computeIfAbsent(message.getAuthor(), m -> new ArrayList<>()).add(message);
        }
        return user_messages;
    }

    public static long generateID() {
        return messages.isEmpty() ? 1 : messages.get(messages.size() - 1).getId() + 1;
    }

    public static String responseMSGIDS(String author, String tag, long id, int limit) {
        StringBuilder response = new StringBuilder("MSG_IDS\r\n");
        ArrayList<Long> ids = new ArrayList<>();
        int limitN = 0;
        System.out.println(author);
        for (Message message : messages) {
            if (message.hasAuthor(author) && message.hasTag(tag) && message.getId() >= id) {
                ids.add(message.getId());
                limitN++;
            }
            if (limitN >= limit) break;
        }
        ids.sort(Collections.reverseOrder());
        for (long ID : ids) {
            response.append(ID).append("\n");
        }
        return response.toString();
    }

    public static String responseMSG(long id) {
        StringBuilder response = new StringBuilder(("MSG\r\n"));
        for (Message message : messages) {
            if (message.getId() == id) {
                response.append("author:")
                        .append(message.getAuthor().getUsername())
                        .append(" msg_id:")
                        .append(id)
                        .append("\r\n");
                response.append(message.getCore());
            }
        }
        return response.toString();
    }


}

