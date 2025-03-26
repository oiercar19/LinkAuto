package com.example.restapi.dto;

import java.util.List;

public class UserDTO {
    private String name;
    private String profilePicture;
    private String email;
    private List<String> cars;
    private long birthDate;
    private String gender;
    private String location;
    private String description;

    // Constructor
    public UserDTO(String name, String profilePicture, String email, List<String> cars, long birthDate, String gender, String location, String description) {
        this.name = name;
        this.profilePicture = profilePicture;
        this.email = email;
        this.cars = cars;
        this.birthDate = birthDate;
        this.gender = gender;
        this.location = location;
        this.description = description;
    }

    // Getters and Setters

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
}