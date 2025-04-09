package com.linkauto.restapi.dto;

public class CommentReturnerDTO {

    private Long id;
    private String text;
    private String username;
    private Long post_id;
    private Long creationDate;

    public CommentReturnerDTO() {
    }

    public CommentReturnerDTO(Long id, String text, String username, Long post_id, Long creationDate) {
        this.id = id;
        this.text = text;
        this.username = username;
        this.post_id = post_id;
        this.creationDate = creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPost_id() {
        return post_id;
    }

    public void setPost_id(Long post_id) {
        this.post_id = post_id;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "CommentReturner{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", username='" + username + '\'' +
                ", post_id='" + post_id + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}