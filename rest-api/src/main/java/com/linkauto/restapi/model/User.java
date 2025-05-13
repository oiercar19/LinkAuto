package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    @Id
    private String username;
    private Role role;
    private boolean banned;
    private String name;
    private String profilePicture;
    private String email;
    private List<String> cars;
    private long birthDate;
    private Gender gender;
    private String location;
    private String password;
    private String description;
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Post> posts;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_followers", joinColumns = @JoinColumn(name = "key_user_username"), inverseJoinColumns = @JoinColumn(name = "follower_username"))
    private List<User> followers;

    @ManyToMany(mappedBy = "followers", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<User> following;

    // No-argument constructor
    public User() {
    }

    // Constructor with arguments
    public User(String username, String name,
            String profilePicture, String email,
            List<String> cars, long birthDate,
            Gender gender, String location,
            String password, String description,List<Post> posts, List<User> followers, List<User> following) {
        this.username = username;
        this.role = Role.USER;
        this.banned = false;
        this.name = name;
        this.profilePicture = profilePicture;
        this.email = email;
        this.cars = cars;
        this.birthDate = birthDate;
        this.gender = gender;
        this.location = location;
        this.password = password;
        this.description = description;
        this.posts = new ArrayList<>();
        for (Post post : posts) {
            this.posts.add(post);
        }
        this.followers = new ArrayList<>();
        for (User follower : followers) {
            this.followers.add(follower);
        }
        this.following = new ArrayList<>();
        for (User follow : following) {
            this.following.add(follow);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getCars() {
        return cars;
    }

    public void setCars(List<String> cars) {
        this.cars = cars;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(long birthDate) {
        this.birthDate = birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean isBanned) {
        this.banned = isBanned;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void addPost(Post post) {
        this.posts.add(post);
    }

    public List<User> getFollowers() {
        return this.followers;
    }

    public void addFollower(User follower) {
        this.followers.add(follower);
    }

    public void removeFollower(User follower) {
        this.followers.remove(follower);
    }

    public List<User> getFollowing() {
        return this.following;
    }

    public void addFollowing(User following) {
        this.following.add(following);
    }

    public void removeFollowing(User following) {
        this.following.remove(following);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", banned="+ banned + ", role=" + role + ", name=" + name + ", profilePicture=" + profilePicture
                + ", email=" + email + ", cars=" + cars + ", birthDate=" + birthDate + ", gender=" + gender
                + ", location=" + location + ", password=" + password + ", description=" + description + ", posts="
                + posts + "]";
    }
    
}
