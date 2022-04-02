package server;

import data.Database;
import data.Message;
import data.User;
import data.UserDB;

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

    public Master(InetSocketAddress address, Database db, int port) {
        super(db);
        localhost = address;
        this.port = port;
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
}
