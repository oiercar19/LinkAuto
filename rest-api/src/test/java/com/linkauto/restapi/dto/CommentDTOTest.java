package com.linkauto.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class CommentDTOTest {

    @Test
    public void testNoArgsConstructor() {
        CommentDTO commentDTO = new CommentDTO();
        assertNull(commentDTO.getText());
    }

    @Test
    public void testAllArgsConstructor() {
        String text = "This is a comment.";
        CommentDTO commentDTO = new CommentDTO(text);
        assertEquals(text, commentDTO.getText());
    }

    @Test
    public void testSettersAndGetters() {
        CommentDTO commentDTO = new CommentDTO();
        String text = "Updated comment text.";
        commentDTO.setText(text);
        assertEquals(text, commentDTO.getText());
    }

    @Test
    public void testToString() {
        String text = "Sample comment";
        CommentDTO commentDTO = new CommentDTO(text);
        String expected = "CommentDTO{text='Sample comment'}";
        assertEquals(expected, commentDTO.toString());
    }
}

