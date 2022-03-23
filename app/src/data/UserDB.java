package data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserDB {
    HashMap<String, User> usernames;
    HashMap<User, List<User>> following;
    HashMap<User, List<User>> followed;
    HashMap<User, List<Long>> messages;

    public UserDB() {
        this.usernames = new HashMap<>();
        this.following = new HashMap<>();
        this.followed = new HashMap<>();
        this.messages = new HashMap<>();
    }

    public User getUserByUsername(String username){
        return usernames.get(username);
    }

    public List<User> getFollowers(User followedUser){
        return followed.get(getUserByUsername(followedUser.getUsername()));
    }

    public List<User> getFollowing(User followingUser){
        return following.get(getUserByUsername(followingUser.getUsername()));
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

    public void addUser(User user){
        String username = user.getUsername();
        if(usernames.containsKey(username)) return;
        usernames.put(username, user);
        following.put(user, new ArrayList<>());
        followed.put(user, new ArrayList<>());
        messages.put(user, new ArrayList<>());
    }

    public void addMessage(User publisher, Long id){
        addUser(publisher);
        this.getMessages(publisher).add(id);
    }

    public List<Long> getMessageById(String user, Long id){
        return messages.get(usernames.get(user));
    }

    public void computeFollow(User follower, User followedUser) throws InvalidParameterException {
        addUser(follower);
        if(this.getFollowers(followedUser) == null) throw new InvalidParameterException("Unknown User");
        this.getFollowers(followedUser).add(followedUser);
        this.getFollowing(follower).add(followedUser);
    }

}
