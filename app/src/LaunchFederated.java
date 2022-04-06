import data.Database;
import server.Master;
import server.Server;
import server.SimpleServer;
import server.Slave;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class LaunchFederated {
    public static void main(String[] args) throws IOException {
        ConfigurationHandler configurationHandler = ConfigurationHandler.newConfiguration("app/src/data/pairs.cfg");
        int port = configurationHandler.getNextPort();
        InetSocketAddress localhost = new InetSocketAddress("localhost", port);
        System.out.println("How much server do you want to start ?");
        Scanner scannerPort = new Scanner(System.in);
        int numberOfServers = scannerPort.nextInt();
        Database database = new Database();
        for (int i = 0; i < numberOfServers; i++) {
            try {
                Server server;

                if(configurationHandler.hasMaster()){
                    server = new Slave(localhost, new InetSocketAddress("localhost", configurationHandler.getMasterPort()), port, database);
                }
                else{
                    server = new Master(localhost, database, port);
                }
                configurationHandler.addServer(server);
                configurationHandler.write();
                server.start();

            } catch (IOException e) {

                e.printStackTrace();
            }

        }
    }
}
