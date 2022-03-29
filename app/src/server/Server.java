package server;

import data.Database;
import data.Message;
import data.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Server {
    final Database db;
    final String POISON_PILL = "!QUIT";
    String ERROR = "ERROR : ";
    String OK = "OK\r\n";
    Selector selector;
    ServerSocketChannel ssc;
    InetSocketAddress localhost;

    public Server() {
        this.db = new Database();
    }

    public abstract void init() throws IOException;

    public abstract void start() throws IOException;

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
}
