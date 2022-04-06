import data.Database;
import server.Master;
import server.Server;
import server.SimpleServer;
import server.Slave;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        ConfigurationHandler configurationHandler = ConfigurationHandler.newConfiguration("app/src/data/pairs.cfg");
        int port = configurationHandler.getNextPort();
        InetSocketAddress localhost = new InetSocketAddress("localhost", port);


        try {
            Server server;
            //server = new SimpleServer();


            if(configurationHandler.hasMaster()){
                server = new Slave(localhost, new InetSocketAddress("localhost", configurationHandler.getMasterPort()), port, new Database());
            }
            else{
                server = new Master(localhost, new Database(), port);
            }
            configurationHandler.addServer(server);
            configurationHandler.write();
            server.start();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
