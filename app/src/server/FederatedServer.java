package server;

import data.Message;
import data.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class FederatedServer extends Server implements RequestHandler{


    ConfigurationHandler configurationHandler;

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
    public void handleConnect(SelectionKey key, SocketChannel client, String result) throws IOException {

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
    public void handlePoisonPill(SocketChannel client) throws IOException {

    }

    @Override
    public void init() throws IOException {
        configurationHandler = ConfigurationHandler.newConfiguration("app/src/data/pairs.cfg");
        port = configurationHandler.getNextPort();
        localhost = new InetSocketAddress("localhost", port);
        configurationHandler.addServer(this);
        configurationHandler.write();

    }

    @Override
    public void start() throws IOException {
        init();
    }

    @Override
    public void notifyFollowers(User author, Message message) {

    }
}
