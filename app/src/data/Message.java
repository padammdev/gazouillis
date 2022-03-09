package data;

import java.util.ArrayList;

public class Message {
    String core;
    long id;
    User author;
    ArrayList<String> tags;

    public Message(String core, long id,  User author) {
        this.core = core;
        this.id = id;
        this.author = author;
        if(core.contains("#")){
            tags = new ArrayList<>();
            String[] split = core.split(" ");
            for(String word : split){
                if(word.charAt(0) == '#' && !tags.contains(word)){
                    tags.add(word);
                }
            }
        }
    }

    public String getCore() {
        return core;
    }

    public long getId() {
        return id;
    }


    public User getAuthor() {
        return author;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag){
        return tag == null || tags.contains(tag);
    }

    public boolean hasAuthor(String username){
        return username == null || author.getUsername().equals(username);
    }

}
