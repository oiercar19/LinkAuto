package com.linkauto.restapi.dto;

import java.util.List;
import java.util.Set;

public class PostReturnerDTO {
    private Long id;
    private String username;      
    private String message;      
    private long creationDate;  
    private List<String> images;
    private List<Long> comment_ids;
    private Set<String> likes;
    
    public PostReturnerDTO() {
    }

    public PostReturnerDTO(Long id, String username, String message, long creationDate, List<String> images, List<Long> comment_ids, Set<String> likes) {
        this.id = id;
        this.username = username;
        this.message = message;
        this.creationDate = creationDate;
        this.images = images;
        this.comment_ids = comment_ids;
        this.likes = likes;
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

    public List<Long> getComment_ids() {
        return comment_ids;
    }

    public void setComment_ids(List<Long> comment_ids) {
        this.comment_ids = comment_ids;
    }

    public Set<String> getLikes() {
        return likes;
    }

    public void setLikes(Set<String> likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "PostReturnerDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", creationDate=" + creationDate +
                ", images=" + images +
                ", comment_ids=" + comment_ids +
                ", likes=" + likes +
                '}';
    }
}
