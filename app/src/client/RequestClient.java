package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class RequestClient {

   String username;
   ByteBuffer buffer;
   SocketChannel client;

   public RequestClient(String username) {
      this.username = username;
   }

   abstract String getCommand();
   abstract void run() throws IOException, InterruptedException;

   void init() throws IOException {
      InetAddress address = InetAddress.getByName("localhost");
      int port = 12345;
      this.client = SocketChannel.open(new InetSocketAddress(address, port));
      this.buffer = ByteBuffer.allocate(1024);
      client.configureBlocking(true);
   }

   void closeConnection() throws IOException {
      String poisonPill = "!QUIT";
      buffer = ByteBuffer.wrap(poisonPill.getBytes());
      client.write(buffer);
      System.out.println("Closing Connexion");
   }

}
