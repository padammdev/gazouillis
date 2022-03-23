package server;

import data.Message;
import data.User;
import data.UserDB;

import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.channels.*;

public class SimpleServer {

    static int usersKey = 0;
    static ArrayList<Long> msgIds = new ArrayList<>();
    static HashMap<Long, Message> idMessage = new HashMap<>();
    static final String POISON_PILL = "!QUIT";
    static ArrayList<Message> messages = new ArrayList<>();
    static ArrayList<String> tags = new ArrayList<>();
    //static HashMap<User, List<Long>> userMessages = new HashMap<>();
    //static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Object> users = new ArrayList<>();
    static HashMap<String, SocketChannel> usernamesClient = new HashMap<>();
    //static HashMap<User, List<User>> listOfFollowers = new HashMap<>();
    static long lastId = 0;
    static final UserDB userDataBase = new UserDB();

    static String ERROR = "ERROR : ";
    static String OK = "OK\r\n";

    public static void main(String[] arg) throws IOException {
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
                System.out.println("Username of connected : " + key.attachment());
                if (key.isAcceptable()) {
                    SocketChannel client = ssc.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    /*if(usernamesClient.containsKey("@hello")){
                        buffer = ByteBuffer.wrap(("You're connected hello").getBytes());
                        usernamesClient.get("@hello").write(buffer);
                    }*/
                    if (client.read(buffer) <= 0){
                        key.cancel();
                    }
                    else {
                        buffer.flip();
                        String result = new String(buffer.array()).trim();
                        System.out.println("Request :" + result);
                        if (result.equals(POISON_PILL)) {
                            client.close();
                            System.out.println("Closing connexion");
                            continue;
                        }
                        String type = Parser.getCommandType(result);

                        switch (type) {

                            /*** PUBLISH ***/

                            case "PUBLISH":
                                HashMap<String, String> command = Parser.parsePublish(result);
                                User author = new User(command.get("author"));
                                long id = generateID();
                                System.out.println("Message id: " + id + " from " + command.get("author"));
                                Message message = new Message(command.get("core"), id, author);
                                if(message.getTags() != null)tags.addAll(message.getTags());

                                userDataBase.addMessage(author, id);

                                idMessage.put(id, message);

                                System.out.println(message.getCore());
                                buffer = ByteBuffer.wrap("OK\r\n".getBytes());
                                client.write(buffer);
                                System.out.println(new String((buffer.array())).trim());

                                //System.out.println(idMessage);
                                break;

                            case "REPLY":
                                HashMap<String, String> command_reply = Parser.parseReply(result);

                                long id_reply = generateID();
                                User user_reply = userDataBase.getUserByUsername(command_reply.get("author"));
                                Message message_reply = new Message(command_reply.get("core"), id_reply, user_reply);

                                messages.add(message_reply);
                                idMessage.put(id_reply,message_reply);



                                /*** verification of the message being replied to ***/
                                if(userDataBase.isUsernameRegistered(idMessage.get(command_reply.get("id")).getAuthor().getUsername())){
                                    userDataBase.addMessage(user_reply, id_reply);
                                    buffer = ByteBuffer.wrap(command_reply.get("core").getBytes());
                                    client.write(buffer);
                                    buffer.clear();
                                    buffer.flip();
                                }

                                buffer = ByteBuffer.wrap("OK\r\n".getBytes());
                                client.write(buffer);
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
                                long idMsg = Long.parseLong(command.get("Msg_id"));
                                if (!idMessage.containsKey(idMsg)) {
                                    buffer = ByteBuffer.wrap((ERROR + "Unknown message id : " +idMsg+ "\r\n").getBytes());
                                } else {
                                    //list_id_message.put(id_msg,message.getCore());
                                    System.out.println(responseMSG(idMsg));
                                    buffer = ByteBuffer.wrap(responseMSG(idMsg).getBytes());
                                }
                                client.write(buffer);

                                break;

                            case "FOLLOW":
                                /* ex : FOLLOW User1 UserFollowed
                                command = Parser.parseFollow(result);
                                User user = Parser.getFirstUser();
                                User user2 = Parser.getUserfollowed();
                                list_of_followers.put(user, m -> new ArrayList<>()).add(user2);
                                System.out.println(user.getUsername() + " follow " + user2.getUsername());
                                */
                                break;

                            case "CONNECT":
                                command = Parser.parseConnect(result);
                                if(userDataBase.isUsernameRegistered(command.get("username"))){
                                    buffer = ByteBuffer.wrap((ERROR + "Username already used\r\n").getBytes(StandardCharsets.UTF_8));
                                }else{
                                    User connectedUser = new User(command.get("username"));
                                    userDataBase.addUser(connectedUser);
                                    buffer = ByteBuffer.wrap((OK).getBytes());
                                }
                                client.write(buffer);

                                key.attach(command.get("username"));
                                users.add(key.attachment());
                                usernamesClient.put(command.get("username"), (SocketChannel) key.channel());
                                System.out.println("Client Connected");
                                System.out.println("Number of users : " + users.size());

                                break;

                            case "REPUBLISH":
                                HashMap<String, String> parserRepublish = Parser.parseRepublish(result);
                                if(userDataBase.isUsernameRegistered(parserRepublish.get("author")) && idMessage.containsKey(Long.parseLong(parserRepublish.get("id")))){
                                    buffer = ByteBuffer.wrap(idMessage.get(Long.parseLong(parserRepublish.get("id"))).getCore().getBytes());
                                    client.write(buffer);
                                }

                                buffer = ByteBuffer.wrap("OK\r\n".getBytes());
                                client.write(buffer);
                                break;

                            case "SUBSCRIBE":
                                command = Parser.parseSubscribe(result);
                                if(command.containsKey("author")){
                                    if(!userDataBase.isUsernameRegistered(command.get("author")))
                                        buffer = ByteBuffer.wrap((ERROR + "User does not exist").getBytes(StandardCharsets.UTF_8));
                                    else {
                                        userDataBase.computeUserFollow(command.get("author"), (String) key.attachment());
                                        buffer = ByteBuffer.wrap((OK).getBytes());
                                    }
                                }
                                else if(command.containsKey("tag")){
                                    if(! tags.contains(command.get("tag"))) tags.add(command.get("tag"));
                                    userDataBase.computeTagFollow(command.get("tag"), userDataBase.getUserByUsername((String) key.attachment()));
                                    buffer = ByteBuffer.wrap((OK).getBytes());
                                }
                                client.write(buffer);
                                break;
                            default:
                                buffer = ByteBuffer.wrap((ERROR + "Unknown command\r\n").getBytes());
                                client.write(buffer);
                                break;
                        }

                        buffer.clear();
                    }
                }
                iterator.remove();
            }

        }


    }

    public static long generateID() {
        return ++lastId;
    }

    public static String responseMSGIDS(String author, String tag, long id, int limit) {
        StringBuilder response = new StringBuilder("MSG_IDS\r\n");
        List<Long> ids = new ArrayList<>();
        List<Long> selected;
        if(userDataBase.getMessages(author)!=null){
            for(long selectedId : userDataBase.getMessages(author)){
                System.out.println(selectedId);
                if(selectedId>=id && idMessage.get(selectedId).hasTag(tag)) ids.add(selectedId);
            }
        }else{
            List<Long> finalIds = ids;
            idMessage.forEach((key, value)->{
                if (value.hasTag(tag) && key >= id) {
                    finalIds.add(key);
                }
            });
            ids.addAll(finalIds);
        }

        ids.sort(Collections.reverseOrder());
        ids = ids.size()>=limit? ids.subList(0, limit) : ids;
        for (long ID : ids) {
            response.append(ID).append("\r\n");
        }
        return response.toString();
    }

    public static String responseMSG(long id) {
        StringBuilder response = new StringBuilder(("MSG\r\n"));
        Message message = idMessage.get(id);
        response.append("author:")
                .append(message.getAuthor().getUsername())
                .append(" msg_id:")
                .append(id)
                .append("\r\n");
        response.append(message.getCore());


        return response.toString();
    }

    public static void notifyFollowers(User author, Message message){

    }



}

