package com.linkauto.restapi.dto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class PostDTOTest {

    @Test
    public void testNoArgsConstructor() {
        PostDTO postDTO = new PostDTO();
        assertNull(postDTO.getMessage());
        assertNull(postDTO.getImages());
    }

    @Test
    public void testAllArgsConstructor() {
        String message = "Test message";
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        PostDTO postDTO = new PostDTO(message, images);

        assertEquals(message, postDTO.getMessage());
        assertEquals(images, postDTO.getImages());
    }

    @Test
    public void testSettersAndGetters() {
        PostDTO postDTO = new PostDTO();

        String message = "Updated message";
        List<String> images = Arrays.asList("image3.jpg", "image4.jpg");

        postDTO.setMessage(message);
        postDTO.setImages(images);

        assertEquals(message, postDTO.getMessage());
        assertEquals(images, postDTO.getImages());
    }
}