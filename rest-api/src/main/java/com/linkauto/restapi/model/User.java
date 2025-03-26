package com.linkauto.restapi.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import  java.util.ArrayList;
import  java.util.List;
import jakarta.persistence.FetchType;


@Entity
@Table(name = "user")
public class User {
    
    public enum Gender {
        MALE,
        FEMALE
      }

    @Id
    private String username;
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

    // No-argument constructor
    public User() {
    }

    //Constructor with arguments
    public User(String username, String name, 
    String profilePicture, String email, 
    List<String> cars, long birthDate, 
    Gender gender, String location, 
    String password, String description, List<Post> posts) {
        this.username = username;
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
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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

    public List<Post> getPosts() {
        return posts;
    }

    public void addPost(Post post) {
        this.posts.add(post);
    }
}
