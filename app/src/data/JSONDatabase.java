package data;
import org.json.*;


import java.io.*;


public class JSONDatabase {

    public static void export(Database db) throws IOException {
        JSONArray database = new JSONArray();
        JSONObject idMessages = new JSONObject(db.getIdMessage());
        JSONArray users = new JSONArray();
        JSONObject lastid = new JSONObject();
        JSONObject usernamesClient = new JSONObject(db.getUsernamesClient());
        JSONArray messages = new JSONArray(db.getMessages());
        JSONArray tags = new JSONArray(db.getTags());
        JSONObject connectedUser = new JSONObject(db.getConnectedUsers());

        lastid.put("lastID", db.getLastId());

        users.put(new JSONObject(db.getUserDB().getUsernames()));
        users.put(new JSONObject(db.getUserDB().getFollowedTags()));

        database.put(idMessages);
        database.put(usernamesClient);
        database.put(connectedUser);
        database.put(lastid);
        database.putAll(users);

        FileWriter fileWriter = new FileWriter("app/src/data/db.json");
        fileWriter.write(database.toString());
        fileWriter.close();
    }

    public static Database importJSON(String filename) throws FileNotFoundException {
        Database db = new Database();
        FileReader fileReader = new FileReader("app/src/data/db.json");
        JSONTokener token = new JSONTokener(fileReader);
        JSONArray database = new JSONArray(token);

        return db;
    }
}
