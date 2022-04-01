import server.FederatedServer;
import server.SimpleServer;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) {
        try {
            new SimpleServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
