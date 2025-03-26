package com.example.restapi.dto;

import java.util.List;

public class PostReturnerDTO {
    private Long id;
    private String username;      
    private String message;      
    private long creationDate;  
    private List<String> images;
    
    public PostReturnerDTO() {
    }

    public PostReturnerDTO(Long id, String username, String message, long creationDate, List<String> images) {
        this.id = id;
        this.username = username;
        this.message = message;
        this.creationDate = creationDate;
        this.images = images;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public List<String> getImages() {
        return images;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "PostReturnerDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", creationDate=" + creationDate +
                ", images=" + images +
                '}';
    }
}
