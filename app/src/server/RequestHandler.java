package server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface RequestHandler {
    void handlePublish(SocketChannel client, String result) throws IOException;
    void handleRCVIDS(SocketChannel client, String result) throws IOException;
    void handleRCVMSG(SocketChannel client, String result) throws IOException;
    void handleConnect(SelectionKey key, SocketChannel client, String result) throws IOException;
    void handleSubscribe(SelectionKey key, SocketChannel client, String result) throws IOException;
    void handleUnsubscribe(SelectionKey key, SocketChannel client, String result) throws IOException;
    void handleReply(SocketChannel client, String result) throws IOException;
    void handleRepublish(SocketChannel client, String result) throws IOException;
    void handlePoisonPill(SocketChannel client) throws IOException;
    void handlePeerRequestID(SocketChannel peer) throws IOException;
    void handleServerConnect(SocketChannel peer) throws IOException;
    void handlePeerRequestUserConnect(SocketChannel peer, String result, SelectionKey key) throws IOException;
}
