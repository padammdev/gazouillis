package server;

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

public class FederatedServer extends Server implements RequestHandler {


    ConfigurationHandler configurationHandler;
    SocketChannel peer;
    boolean isMaster;

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
        if (isMaster) {
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
        else{
            peer.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
        }


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
        configurationHandler = ConfigurationHandler.newConfiguration("app/src/data/pairs.cfg");
        port = configurationHandler.getNextPort();
        localhost = new InetSocketAddress("localhost", port);
        configurationHandler.addServer(this);
        configurationHandler.write();
        isMaster = configurationHandler.isMaster(this);
        ssc = ServerSocketChannel.open();
        ssc.bind(localhost);
        ssc.configureBlocking(false);
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server ok");
        if(!isMaster) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", configurationHandler.getMasterPort());
            peer = SocketChannel.open(inetSocketAddress);
            peer.configureBlocking(false);
        }

    }


    @Override
    public void notifyFollowers(User author, Message message) {

    }
}
