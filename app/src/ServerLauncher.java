import server.FederatedServer;
import server.SimpleServer;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) {

        try {
            //new SimpleServer().start();
            new FederatedServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
