package server;

import data.Database;
import data.Message;
import data.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Slave extends Server{

    SocketChannel toMaster;
    SocketAddress masterAddress;

    public Slave(InetSocketAddress address, SocketAddress masterAddress, int port, Database db) throws IOException {
        super(db);
        localhost = address;
        this.port = port;
        this.masterAddress = masterAddress;
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

    }

    @Override
    public void handleServerConnect(SocketChannel peer) throws IOException {

    }

    @Override
    public void handlePeerRequestUserConnect(SocketChannel peer, String result, SelectionKey key) throws IOException {

    }

    @Override
    public void init() throws IOException {
        toMaster = SocketChannel.open(masterAddress);
        toMaster.configureBlocking(true);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String register = "SERVERCONNECT" + "\r\n";
        buffer.put(register.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        toMaster.write(buffer);
        buffer.clear();
        toMaster.read(buffer);
        buffer.flip();
        String response = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();
        System.out.println(response);
        buffer.flip();


        ssc = ServerSocketChannel.open();
        ssc.bind(localhost);
        ssc.configureBlocking(false);
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server" + response);
    }

    @Override
    public void handleConnect(SelectionKey key, SocketChannel client, String result) throws IOException {
        HashMap<String, String> parsedCommand = Parser.parseConnect(result);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String register = "PEER_USERCONNECT username:"+parsedCommand.get("username") + "\r\n";
        buffer.put(register.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        toMaster.write(buffer);
        buffer.clear();
        toMaster.read(buffer);
        buffer.flip();
        String response = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();
        System.out.println(response);
        buffer.flip();
        if(response.contains(OK)){
            key.attach(parsedCommand.get("username"));
            sendOK(client);
        }
        else{
            client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        }

    }

    @Override
    public void notifyFollowers(User author, Message message) {

    }
}
