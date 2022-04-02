import data.Database;
import server.Master;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        ConfigurationHandler configurationHandler = ConfigurationHandler.newConfiguration("app/src/data/pairs.cfg");
        int port = configurationHandler.getNextPort();
        InetSocketAddress localhost = new InetSocketAddress("localhost", port);

        try {
            //new SimpleServer().start();

            new Master(localhost, new Database(), port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
