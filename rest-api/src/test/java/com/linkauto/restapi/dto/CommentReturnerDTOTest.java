package com.linkauto.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class CommentReturnerDTOTest {

    @Test
    public void testNoArgsConstructor() {
        CommentReturnerDTO comment = new CommentReturnerDTO();
        assertNull(comment.getId());
        assertNull(comment.getText());
        assertNull(comment.getUsername());
        assertNull(comment.getPost_id());
        assertNull(comment.getCreationDate());
    }

    @Test
    public void testAllArgsConstructor() {
        Long id = 1L;
        String text = "Nice post!";
        String username = "user123";
        Long postId = 99L;
        Long creationDate = 1680000000000L;

        CommentReturnerDTO comment = new CommentReturnerDTO(id, text, username, postId, creationDate);

        assertEquals(id, comment.getId());
        assertEquals(text, comment.getText());
        assertEquals(username, comment.getUsername());
        assertEquals(postId, comment.getPost_id());
        assertEquals(creationDate, comment.getCreationDate());
    }

    @Test
    public void testSettersAndGetters() {
        CommentReturnerDTO comment = new CommentReturnerDTO();

        Long id = 5L;
        String text = "Test comment";
        String username = "tester";
        Long postId = 101L;
        Long creationDate = 1700000000000L;

        comment.setId(id);
        comment.setText(text);
        comment.setUsername(username);
        comment.setPost_id(postId);
        comment.setCreationDate(creationDate);

        assertEquals(id, comment.getId());
        assertEquals(text, comment.getText());
        assertEquals(username, comment.getUsername());
        assertEquals(postId, comment.getPost_id());
        assertEquals(creationDate, comment.getCreationDate());
    }

    @Test
    public void testToString() {
        CommentReturnerDTO comment = new CommentReturnerDTO(1L, "Example text", "user1", 10L, 1680000000000L);
        String expected = "CommentReturner{id=1, text='Example text', username='user1', post_id='10', creationDate=1680000000000}";
        assertEquals(expected, comment.toString());
    }
}

