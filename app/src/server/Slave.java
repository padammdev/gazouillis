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
        String response = writeToMaster(result);
        if(response.contains(OK)){
            sendOK(client);
        }
        else{
            client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Override
    public void handleRCVIDS(SocketChannel client, String result) throws IOException {
        String response = writeToMaster(result);
        client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void handleRCVMSG(SocketChannel client, String result) throws IOException {
        String response = writeToMaster(result);
        client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void handleSubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {
        HashMap<String, String> parsedCommand = Parser.parseSubscribe(result);
        String request = "PEER_SUBSCRIBE user:"+ (String) key.attachment();
        if(parsedCommand.containsKey("tag")) request +=" toTag:" + parsedCommand.get("tag") + "\r\n";
        else if(parsedCommand.containsKey("user")) request += " to:" + parsedCommand.get("user") + "\r\n";
        String response = writeToMaster(request);
        client.write(ByteBuffer.wrap(response.getBytes()));
    }

    @Override
    public void handleUnsubscribe(SelectionKey key, SocketChannel client, String result) throws IOException {
        HashMap<String, String> parsedCommand = Parser.parseSubscribe(result);
        String request = "PEER_UNSUBSCRIBE user:"+ (String) key.attachment();
        if(parsedCommand.containsKey("tag")) request +=" toTag:" + parsedCommand.get("tag") + "\r\n";
        else if(parsedCommand.containsKey("user")) request += " to:" + parsedCommand.get("user") + "\r\n";
        String response = writeToMaster(request);
        client.write(ByteBuffer.wrap(response.getBytes()));
    }

    @Override
    public void handleReply(SocketChannel client, String result) throws IOException {
        String response = writeToMaster(result);
        client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void handleRepublish(SocketChannel client, String result) throws IOException {
        String response = writeToMaster(result);
        client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void handleServerConnect(SocketChannel peer) throws IOException {

    }

    @Override
    public void handlePeerRequestUserConnect(SocketChannel peer, String result, SelectionKey key) throws IOException {

    }

    @Override
    public void handlePeerRequestSubscribe(SocketChannel peer, String result) throws IOException {

    }

    @Override
    public void handlePeerRequestUnsubscribe(SocketChannel peer, String result) throws IOException {

    }

    @Override
    public void handleNotificationRequest(String result) {
        HashMap<String, String> parsedCommand = Parser.parseNotificationRequest(result);
    }

    @Override
    public void init() throws IOException {
        toMaster = SocketChannel.open(masterAddress);
        toMaster.configureBlocking(true);
        String register = "SERVERCONNECT" + "\r\n";
        String response = writeToMaster(register);
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
        String register = "PEER_USERCONNECT username:"+parsedCommand.get("username") + "\r\n";
        String response = writeToMaster(register);
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

    private String writeToMaster(String result) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(result.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        toMaster.write(buffer);
        buffer.clear();
        toMaster.read(buffer);
        buffer.flip();
        String response = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();
        System.out.println(response);
        buffer.flip();
        return response;
    }
}
