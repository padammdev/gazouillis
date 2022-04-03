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
import java.util.HashMap;

public class Master extends Server {

    HashMap<Integer, SocketChannel> slaves;

    public Master(InetSocketAddress address, Database db, int port) {
        super(db);
        localhost = address;
        this.port = port;
        this.slaves = new HashMap<>();
    }

    @Override
    public void handlePublish(SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleRCVIDS(SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleRCVMSG(SocketChannel client, String result) throws IOException {

    }


    @Override
    public void handleSubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleUnsubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleReply(SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handleRepublish(SocketChannel client, String result) throws IOException {

    }

    @Override
    public void handlePeerRequestID(SocketChannel peer) throws IOException {
        long id = db.generateID();
        peer.write(ByteBuffer.wrap(("ID\r\n" + id + "\r\n").getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void handleServerConnect(SocketChannel peer) throws IOException {
        this.slaves.put(getPeerPort(peer), peer);
        sendOK(peer);
    }

    @Override
    public void handlePeerRequestUserConnect(SocketChannel peer, String result, SelectionKey key) throws IOException {
        HashMap<String, String> command = Parser.parseConnect(result);
        System.out.println("proceeding uqer connect");
        if (db.getUserDB().isUsernameRegistered(command.get("username"))) {
            sendERROR(peer, "Username already used\r\n");
        } else {
            User connectedUser = new User(command.get("username"));
            db.getUserDB().addUser(connectedUser);
            db.getUsernamesClient().put(command.get("username"), (SocketChannel) key.channel());
            db.getConnectedUsers().put(connectedUser, getPeerPort(peer));
            sendOK(peer);
        }
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

    }

    public int getPeerPort(SocketChannel peer) throws IOException {
        return ((InetSocketAddress) peer.getRemoteAddress()).getPort();
    }
}
