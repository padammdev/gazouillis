package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserDB {
    HashMap<String, User> usernames;
    HashMap<User, List<Long>> messages;
    HashMap<String, List<User>> followedTags;
    

    public UserDB() {
        this.usernames = new HashMap<>();
        this.messages = new HashMap<>();
        this.followedTags = new HashMap<>();
    }

    public User getUserByUsername(String username){
        return usernames.get(username);
    }


    public List<Long> getMessages(User publisher){
        return messages.get(getUserByUsername(publisher.getUsername()));
    }

    public List<Long> getMessages(String author){
        return messages.get(getUserByUsername(author));
    }

    public boolean isUsernameRegistered(String username){
        return usernames.containsKey(username);
    }

    public List<String> getFollowersUsernames(User author){
        List<String> followersUsernames = new ArrayList<>();
        for(User follower : author.getFollowers()){
            followersUsernames.add(follower.getUsername());
        }
        return followersUsernames;
    }

    public void addUser(User user){
        String username = user.getUsername();
        if(usernames.containsKey(username)) return;
        usernames.put(username, user);
        messages.put(user, new ArrayList<>());
    }

    public void addMessage(User publisher, Long id){
        addUser(publisher);
        this.getMessages(publisher).add(id);
    }

    public List<Long> getMessageById(String user, Long id){
        return messages.get(usernames.get(user));
    }

    public void computeUserFollow(String followedName, String followerName){
        User followed = this.getUserByUsername(followedName);
        User follower = this.getUserByUsername(followerName);
        followed.addFollower(follower);
        follower.addFollow(followed);
    }

    public void computeTagFollow(String tag, User user){
        followedTags.computeIfAbsent(tag, list -> new ArrayList<>());
        followedTags.get(tag).add(user);
    }

    public void computeTagUnfollow(String tag, User user){
        followedTags.get(tag).remove(user);
    }

    public void computeUserUnfollow(String followedName, String followerName){
        User followed = this.getUserByUsername(followedName);
        User follower = this.getUserByUsername(followerName);
        followed.removeFollower(follower);
        follower.removeFollow(followed);
    }

    public boolean isTagFollowed(String tag, User user){
        return followedTags.containsKey(tag) && followedTags.get(tag).contains(user);
    }

    public List<String> getTagFollowersUsernames(String tag){
        List<String> usernames = new ArrayList<>();
        for(User user : followedTags.get(tag)){
            usernames.add(user.getUsername());
        }
        return usernames;
    }

    @Override
    public String toString() {
        return "UserDB{" +
               "usernames=" + usernames +
               ", messages=" + messages +
               ", followedTags=" + followedTags +
               '}';
    }
}
