package com.linkauto.restapi.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class PostReturnerDTOTest {

    @Test
    public void testNoArgsConstructor() {
        PostReturnerDTO dto = new PostReturnerDTO();
        assertNotNull(dto);
    }

    @Test
    public void testAllArgsConstructor() {
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        List<Long> commentIds = Arrays.asList(1L, 2L, 3L);
        Set<String> likes = new HashSet<>(Arrays.asList("user1", "user2"));

        PostReturnerDTO dto = new PostReturnerDTO(1L, "testUser", "Test message", 123456789L, images, commentIds, likes);

        assertEquals(1L, dto.getId());
        assertEquals("testUser", dto.getUsername());
        assertEquals("Test message", dto.getMessage());
        assertEquals(123456789L, dto.getCreationDate());
        assertEquals(images, dto.getImages());
        assertEquals(commentIds, dto.getComment_ids());
        assertEquals(likes, dto.getLikes());
    }

    @Test
    public void testSettersAndGetters() {
        PostReturnerDTO dto = new PostReturnerDTO();

        dto.setId(2L);
        dto.setUsername("newUser");
        dto.setMessage("New message");
        dto.setCreationDate(987654321L);
        dto.setImages(Arrays.asList("image3.jpg", "image4.jpg"));
        dto.setComment_ids(Arrays.asList(4L, 5L));

        assertEquals(2L, dto.getId());
        assertEquals("newUser", dto.getUsername());
        assertEquals("New message", dto.getMessage());
        assertEquals(987654321L, dto.getCreationDate());
        assertEquals(Arrays.asList("image3.jpg", "image4.jpg"), dto.getImages());
        assertEquals(Arrays.asList(4L, 5L), dto.getComment_ids());
    }

    @Test
    public void testToString() {
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        List<Long> commentIds = Arrays.asList(1L, 2L, 3L);
        Set<String> likes = new HashSet<>(Arrays.asList("user1", "user2"));

        PostReturnerDTO dto = new PostReturnerDTO(1L, "testUser", "Test message", 123456789L, images, commentIds, likes);

        String expected = "PostReturnerDTO{id=1, username='testUser', message='Test message', creationDate=123456789, images=[image1.jpg, image2.jpg], comment_ids=[1, 2, 3], likes=[user1, user2]}";
        assertEquals(expected, dto.toString());
    }
}