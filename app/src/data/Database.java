package data;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    HashMap<Long, Message> idMessage;
    UserDB userDB;
    long lastId = 0;
    HashMap<String, SocketChannel> usernamesClient;
    ArrayList<Message> messages;
    ArrayList<String> tags;
    HashMap<User, Integer> connectedUsers;

    public Database() {
        this.idMessage = new HashMap<>();
        this.userDB = new UserDB();
        this.lastId = 0;
        this.usernamesClient = new HashMap<>();
        this.messages = new ArrayList<>();
        this.tags = new ArrayList<>();
        connectedUsers = new HashMap<>();
    }

    public HashMap<Long, Message> getIdMessage() {
        return idMessage;
    }

    public UserDB getUserDB() {
        return userDB;
    }

    public long getLastId() {
        return lastId;
    }

    public HashMap<String, SocketChannel> getUsernamesClient() {
        return usernamesClient;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public long generateID() {
        return ++lastId;
    }

    public HashMap<User, Integer> getConnectedUsers() {
        return connectedUsers;
    }

    public void addConnection(User user, int port){
        connectedUsers.put(user, port);
    }

    public boolean isUserConnectedToSlave(User user){
        return connectedUsers.containsKey(user);
    }
}
