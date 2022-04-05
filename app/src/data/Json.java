package data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.FileWriter;
import java.io.IOException;

public class Json {
    JSONObject userdetail = new JSONObject();
    JSONArray userList = new JSONArray();
    UserDB userDB;

    public Json() throws IOException {
    }

    //add user if he doesn't exit yet
    public void addUser(User user){
        JSONObject userObject = new JSONObject();
        userdetail.put(user.getUsername(), new JSONArray());
    }

    public void addMessage(User user){
        userdetail.put(user.getUsername(), userDB.getMessages(user.getUsername()));
    }


    public void write() throws IOException {
        FileWriter file = new FileWriter("C:\\\\Users\\\\badro\\\\Desktop\\\\gazouillis\\\\app\\\\src\\\\data\\\\Json.json");
        file.write(userList.toString());
        file.flush();
    }




    //add message published

    //add follower

    //remove follower

    // for each tag

}


