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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Master extends Server {

    HashMap<Integer, SocketChannel> slaves;

    public Master(InetSocketAddress address, Database db, int port) {
        super(db);
        localhost = address;
        this.port = port;
        this.slaves = new HashMap<>();
    }





    @Override
    public void handleSubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleUnsubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleServerConnect(SocketChannel peer) throws IOException {
        this.slaves.put(getPeerPort(peer), peer);
        sendOK(peer);
    }

    @Override
    public void handlePeerRequestUserConnect(SocketChannel peer, String result, SelectionKey key) throws IOException {
        HashMap<String, String> command = Parser.parseConnect(result);
        if (db.getUserDB().isUsernameRegistered(command.get("username"))) {
            sendERROR(peer, "Username already used\r\n");
        } else {
            User connectedUser = new User(command.get("username"));
            db.getUserDB().addUser(connectedUser);
            db.getUsernamesClient().put(command.get("username"), (SocketChannel) key.channel());
            db.addConnection(connectedUser, getPeerPort(peer));
            sendOK(peer);
        }
    }

    @Override
    public void handlePeerRequestSubscribe(SocketChannel peer, String result) throws IOException {
        HashMap<String, String> command = Parser.parsePeerRequestSubscribe(result);
        if(command.containsKey("toTag")){
            if (!db.getTags().contains(command.get("toTag"))) db.getTags().add(command.get("toTag"));
            db.getUserDB().computeTagFollow(command.get("toTag"), db.getUserDB().getUserByUsername(command.get("user")));
            sendOK(peer);
        }else{
            if (!db.getUserDB().isUsernameRegistered(command.get("user")) || ! db.getUserDB().isUsernameRegistered(command.get("to")))
                sendERROR(peer, "Username does not exist\r\n");
            else {
                db.getUserDB().computeUserFollow(command.get("user"), command.get("to"));
                sendOK(peer);
            }
        }
    }

    @Override
    public void handlePeerRequestUnsubscribe(SocketChannel peer, String result) throws IOException {
        HashMap<String, String> command = Parser.parsePeerRequestSubscribe(result);
        if(command.containsKey("toTag")){
            if (!db.getTags().contains(command.get("toTag"))) db.getTags().add(command.get("toTag"));
            db.getUserDB().computeTagUnfollow(command.get("toTag"), db.getUserDB().getUserByUsername(command.get("user")));
            sendOK(peer);
        }else{
            if (!db.getUserDB().isUsernameRegistered(command.get("user")))
                sendERROR(peer, "Username does not exist\r\n");
            else {
                db.getUserDB().computeUserUnfollow(command.get("user"), command.get("to"));
                sendOK(peer);
            }
        }
    }

    @Override
    public void handleNotificationRequest(String result) {

    }

    @Override
    public void init() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.bind(localhost);
        ssc.configureBlocking(false);
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server ok");
    }


    @Override
    public void notifyFollowers(User author, Message message) {
        List<String> followersUsernames = db.getUserDB().getFollowersUsernames(author);
        List<String> tagFollowersUsernames = new ArrayList<>();
        for (String tag : message.getTags()) {
            tagFollowersUsernames.addAll(db.getUserDB().getTagFollowersUsernames(tag));
        }
        for (String username : followersUsernames) {
            if(db.isUserConnectedToSlave(db.getUserDB().getUserByUsername(username)))sendNotificationRequest(message, username);
            else{
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
        for (String username : tagFollowersUsernames) {
            if (followersUsernames.contains(username)) continue;
            if(db.isUserConnectedToSlave(db.getUserDB().getUserByUsername(username))) sendNotificationRequest(message, username);
            else{
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

    private void sendNotificationRequest(Message message, String username) {
        SocketChannel slave = slaves.get(db.getConnectedUsers().get(db.getUserDB().getUserByUsername(username)));
        String response = "NOTIFY user:" + username + "\r\n" + responseMSG(message.getId());
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        try {
            slave.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.clear();
    }

    public int getPeerPort(SocketChannel peer) throws IOException {
        return ((InetSocketAddress) peer.getRemoteAddress()).getPort();
    }

    private boolean isSlave(SocketChannel slave){
        return slaves.containsValue(slave);
    }
}
