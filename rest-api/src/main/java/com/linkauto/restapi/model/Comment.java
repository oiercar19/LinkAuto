package com.linkauto.restapi.model;
//import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the comment
    private String text;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", nullable = false)
    private User user; // username of the user who made the comment
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    private Long creationDate;

    public Comment() {
    }

    public Comment(String text, User user, Post post, Long creationDate) {
        this.text = text;
        this.user = user;
        this.post = post;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", user=" + user +
                '}';
    }

}
