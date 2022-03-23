package server;

import java.util.HashMap;

public class Parser {
    public static HashMap<String, String> parsePublish(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String[] commandSplit = command.split("\r\n");
        String[] header = commandSplit[0].split(" ");
        String core = commandSplit.length >1? commandSplit[1]: "";

        parsedCommand.put("Type", header[0]);
        parsedCommand.put("author", header[1].substring(header[1].indexOf("@")));

        parsedCommand.put("core", core);

        return parsedCommand;
    }

    public static HashMap<String, String> parseRepublish(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String [] commandSplit = command.split("\r\n");
        String [] header = commandSplit[0].split(" ");
        String id = commandSplit[3];

        System.out.println(commandSplit + " " + header + " " + id);

        parsedCommand.put("Type", header[0]);
        parsedCommand.put("author", header[1].substring(header[1].indexOf("@")));
        parsedCommand.put("id", id);

        return parsedCommand;
    }
    public static HashMap<String, String> parseRCVIDS(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String[] commandSplit = command.split("\r\n");
        String[] header = commandSplit[0].split(" ");
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
        String[] commandSplit = command.split("\r\n");
        String[] header = commandSplit[0].split(" ");
        parsedCommand.put("Type", header[0]);
        parsedCommand.put("Msg_id", header[1].substring(header[1].indexOf(":")+1));
        return parsedCommand;
    }

    public static String getCommandType(String command){
        return command.split("\r\n")[0].split(" ")[0];
    }

    public static HashMap<String, String> parseRegister(String command){
        HashMap<String, String > parsedCommand = new HashMap<>();
        String[] commandSplit = command.split("\r\n");
        String[] header = commandSplit[0].split(" ");
        parsedCommand.put("Type", header[0]);
        parsedCommand.put("username", header[1].substring(header[1].indexOf("@")));
        return parsedCommand;
    }

    public static HashMap<String, String> parseSubscribe(String command){
        HashMap<String, String> parsedCommand = new HashMap<>();
        String[] commandSplit = command.split("\r\n");
        String[] header = commandSplit[0].split(" ");
        parsedCommand.put("Type", header[0]);
        if(header[1].contains("author")) parsedCommand.put("author", header[1].substring(header[1].indexOf("@")));
        if (header[1].contains("tag")) parsedCommand.put("tag", header[1].substring(header[1].indexOf("#")));
        return parsedCommand;
    }
}
