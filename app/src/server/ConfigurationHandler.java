package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Scanner;

public class ConfigurationHandler {

    File cfg;
    ArrayList<String> configs;
    int nextPort;

    static ConfigurationHandler newConfiguration(String filename) throws IOException {
        ConfigurationHandler configurationHandler = new ConfigurationHandler();
        configurationHandler.cfg = new File(filename);
        configurationHandler.configs = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filename));
        int port = 12345;
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if( ! line.equals("")) {
                configurationHandler.configs.add(line);
                port = Integer.parseInt(line.split(" ")[3]) + 1;
            }
        }
        configurationHandler.setNextPort(port);
        return configurationHandler;
    }


    boolean isMaster() {
        return configs.isEmpty();
    }

    boolean isMaster(Server server){
        return configs.contains("master = "+ server.localhost.getAddress().toString() + " " + server.port);
    }

    boolean isRegistered(Server server){
        return isMaster(server) || configs.contains("peer = " + server.localhost.getAddress().toString() + " " + server.port);
    }

    void write() throws IOException {
        FileWriter writer = new FileWriter(cfg);
        for(String config : configs){
            writer.write(config + "\r\n");
        }
        writer.close();
    }

    void addServer(Server server){
        String address = server.localhost.getAddress().toString();
        if(configs.isEmpty()){
            configs.add("master = "+ address + " " + server.port);
        }else{
            configs.add("peer = " + address + " " + server.port);
        }
    }
    void removeServer(Server server){
        if(isMaster(server)) throw new InvalidParameterException("Cannot remove master.");
        else{
            String verifier = "peer = " + server.localhost.getAddress().toString() + " " + server.port;
            for(String config : configs){
                if(config.equals(verifier)) configs.remove(verifier);
            }
        }
    }

    int getMasterPort(){
        return 12345;
    }

    int getNextPort(){
        return nextPort;
    }

    public void setNextPort(int nextPort) {
        this.nextPort = nextPort;
    }
}
