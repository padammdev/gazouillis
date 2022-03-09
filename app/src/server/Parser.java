package server;

import java.util.Arrays;
import java.util.HashMap;

public class Parser {
    public static HashMap<String, String> parsePublish(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String[] command_split = command.split("\r\n");
        String[] header = command_split[0].split(" ");
        String core = command_split.length >1? command_split[1]: "";

        parsedCommand.put("Type", header[0]);
        parsedCommand.put("author", header[1].substring(header[1].indexOf("@")));

        parsedCommand.put("core", core);

        return parsedCommand;
    }
    public static HashMap<String, String> parseRCVIDS(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String[] command_split = command.split("\r\n");
        String[] header = command_split[0].split(" ");
        parsedCommand.put("Type", header[0]);
        for(String option : header){
            parsedCommand.put("author", option.contains("author") ? option.substring(option.indexOf("@")) : null);
            parsedCommand.put("tag", option.contains("tag") ? option.substring(option.indexOf("#")) : null);
            parsedCommand.put("sinceId", option.contains("since_id") ? option.substring(option.indexOf("since_id:")+1) : null);
            parsedCommand.put("limit", option.contains("limit") ? option.substring(option.indexOf("limit:")+1) : null);
        }

        return parsedCommand;
    }
    public static HashMap<String, String> parseRCVMSG(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String[] command_split = command.split("\r\n");
        String[] header = command_split[0].split(" ");
        parsedCommand.put("Type", header[0]);
        parsedCommand.put("Msg_id", header[1].substring(header[1].indexOf(":")+1));
        return parsedCommand;
    }

    public static String getCommandType(String command){
        return command.split("\r\n")[0].split(" ")[0];
    }
}
