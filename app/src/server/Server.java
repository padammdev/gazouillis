package server;

import data.Database;
import data.Message;
import data.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class Server implements RequestHandler{
    final Database db;
    final String POISON_PILL = "!QUIT";
    String ERROR = "ERROR : ";
    String OK = "OK\r\n";
    Selector selector;
    ServerSocketChannel ssc;
    InetSocketAddress localhost;
    int port;

    public Server() {
        this.db = new Database();
    }

    public Server(Database db){
        this.db = db;
    }

    public abstract void init() throws IOException;

    public void start() throws IOException{
        init();

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

                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    if (client.read(buffer) <= 0) {
                        key.cancel();
                    } else {
                        buffer.flip();
                        String result = new String(buffer.array()).trim();
                        System.out.println("Request :" + result);

                        if (result.equals(POISON_PILL)) {
                            handlePoisonPill(client);
                            continue;
                        }
                        String type = Parser.getCommandType(result);

                        switch (type) {
                            case "PUBLISH":
                                this.handlePublish(client, result);
                                break;
                            case "REPLY":
                                this.handleReply(client, result);
                                break;
                            case "RCV_IDS":
                                this.handleRCVIDS(client, result);
                                break;
                            case "RCV_MSG":
                                this.handleRCVMSG(client, result);
                                break;
                            case "CONNECT":
                                this.handleConnect(key, client, result);
                                break;
                            case "REPUBLISH":
                                this.handleRepublish(client, result);
                                break;
                            case "SUBSCRIBE":
                                this.handleSubscribe(key, client, result);
                                break;
                            case "UNSUBSCRIBE":
                                this.handleUnsubscribe(key, client, result);
                                break;
                            case "SERVERCONNECT":
                                this.handleServerConnect(client);
                                break;
                            case "PEER_REQUESTID":
                                this.handlePeerRequestID(client);
                                break;
                            case "PEER_USERCONNECT":
                                this.handlePeerRequestUserConnect(client, result, key);
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

    public long generateID() {
        return db.generateID();
    }

    public String responseMSGIDS(String author, String tag, long id, int limit) {
        StringBuilder response = new StringBuilder("MSG_IDS\r\n");
        List<Long> ids = new ArrayList<>();
        if (db.getUserDB().getMessages(author) != null) {
            for (long selectedId : db.getUserDB().getMessages(author)) {
                System.out.println(selectedId);
                if (selectedId >= id && db.getIdMessage().get(selectedId).hasTag(tag)) ids.add(selectedId);
            }
        } else {
            List<Long> finalIds = ids;
            db.getIdMessage().forEach((key, value) -> {
                if (value.hasTag(tag) && key >= id) {
                    finalIds.add(key);
                }
            });
            ids.addAll(finalIds);
        }

        ids.sort(Collections.reverseOrder());
        ids = ids.size() >= limit ? ids.subList(0, limit) : ids;
        for (long ID : ids) {
            response.append(ID).append("\r\n");
        }
        return response.toString();
    }

    public String responseMSG(long id) {
        StringBuilder response = new StringBuilder(("MSG\r\n"));
        Message message = db.getIdMessage().get(id);
        response.append("author:")
                .append(message.getAuthor().getUsername())
                .append(" msg_id:")
                .append(id)
                .append("\r\n");
        response.append(message.getCore());


        return response.toString();
    }

    public abstract void notifyFollowers(User author, Message message);

    public void sendOK(SocketChannel client){
        try {
            client.write(ByteBuffer.wrap(OK.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendERROR(SocketChannel client, String error){
        try {
            client.write(ByteBuffer.wrap((ERROR + error).getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void handlePoisonPill(SocketChannel client) throws IOException {
        client.close();
        System.out.println("Closing connexion");
    }

    @Override
    public void handleConnect(SelectionKey key, SocketChannel client, String result) throws IOException {

        HashMap<String, String> command = Parser.parseConnect(result);
        if (db.getUserDB().isUsernameRegistered(command.get("username"))) {
            sendERROR(client, "Username already used\r\n");
        } else {
            User connectedUser = new User(command.get("username"));
            db.getUserDB().addUser(connectedUser);
            sendOK(client);
        }

        key.attach(command.get("username"));
        db.getUsernamesClient().put(command.get("username"), (SocketChannel) key.channel());
        System.out.println("Client Connected");

    }

    public InetSocketAddress getLocalhost() {
        return localhost;
    }

    public int getPort() {
        return port;
    }
}
