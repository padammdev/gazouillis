package data;

import java.util.*;

public class User {
    String username;
    HashSet<User> follows;
    HashSet<User> followers;

    public User(String username) {
        this.username = username;
        follows = new HashSet<>();
        followers = new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return this.getUsername().equals(user.getUsername());
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", follows=" + follows +
               ", followers=" + followers +
               '}';
    }

    public void addFollow(User user){
        this.follows.add(user);
    }

    public void addFollower(User user){
        this.followers.add(user);
    }

    public void addTag(String tag){
    }

    public boolean isFollowing(User user){
        return follows.contains(user);
    }

    public boolean isFollowedBy(User user){
        return followers.contains(user);
    }

    public HashSet<User> getFollows() {
        return follows;
    }

    public HashSet<User> getFollowers() {
        return followers;
    }

    public void removeFollower(User user){
        this.followers.remove(user);
    }

    public void removeFollow(User user){
        this.follows.remove(user);
    }
}
