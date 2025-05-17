package com.linkauto.restapi.dto;

import java.util.ArrayList;
import java.util.List;

public class UserReturnerDTO {
    private String username;
    private String role;
    private String name;
    private String profilePicture;
    private String email;
    private List<String> cars;
    private long birthDate;
    private String gender;
    private String location;
    private String password;
    private String description;
    private List<PostReturnerDTO> posts;
    private Boolean isVerified;
    private List<PostReturnerDTO> savedPost;

    // Constructor
    public UserReturnerDTO(String username, String role , String name, String profilePicture, String email, List<String> cars, long birthDate, String gender, String location, String password, String description, List<PostReturnerDTO> posts, List<PostReturnerDTO> savedPost, Boolean isVerified) {
        this.username = username;
        this.role = role;
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
        for (PostReturnerDTO post : posts){
            this.posts.add(post);
        }
        this.savedPost = new ArrayList<>();
        for (PostReturnerDTO post : savedPost){
            this.savedPost.add(post);
        }
        this.isVerified = isVerified;
    }

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<PostReturnerDTO> getPosts(){
        return this.posts;
    }

    public void addPost(PostReturnerDTO post){
        this.posts.add(post);
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public List<PostReturnerDTO> getSavedPost() {
        return savedPost;
    }

    public void addSavedPost(PostReturnerDTO post) {
        this.savedPost.add(post);
    }
}