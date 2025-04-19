package com.linkauto.restapi.model;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommentTest {
    
    private Comment comment;

    @BeforeEach
    public void setUp() {
        comment = new Comment("test comment", new User(), new Post(), 999999999L);
    }

    @Test
    public void testGettersAndSetters() {
        User user = new User();
        Post post = new Post();

        assertEquals("test comment", comment.getText());
        assertEquals(user, comment.getUser());
        assertEquals(post, comment.getPost());
        assertEquals(999999999L, comment.getCreationDate());

        comment.setText("updated comment");
        assertEquals("updated comment", comment.getText());

        comment.setCreationDate(123456789L);
        assertEquals(123456789L, comment.getCreationDate());
    }

    @Test
    public void testSetUser() {
        User user = new User();
        comment.setUser(user);
        assertEquals(user, comment.getUser());
    }

    @Test
    public void testSetPost() {
        Post post = new Post();
        comment.setPost(post);
        assertEquals(post, comment.getPost());
    }
    @Test
    void testToString() {
        String expected = "Comment{" +
            "text='" + comment.getText() + '\'' +
            ", user=" + comment.getUser() +
            '}';
        assertEquals(expected, comment.toString());
    }
}
