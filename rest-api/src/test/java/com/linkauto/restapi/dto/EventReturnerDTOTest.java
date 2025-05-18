package com.linkauto.restapi.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class EventReturnerDTOTest {

    @Test
    public void testNoArgsConstructor() {
        EventReturnerDTO dto = new EventReturnerDTO();
        assertNotNull(dto);
    }

    @Test
    public void testAllArgsConstructor() {
        Long id = 1L;
        String username = "testUser";
        String title = "Test Event";
        String description = "Test Description";
        String location = "Test Location";
        long startDate = 1621234567890L;
        long endDate = 1621334567890L;
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        Set<String> participants = new HashSet<>(Arrays.asList("user1", "user2"));
        List<Long> commentIds = Arrays.asList(1L, 2L, 3L);

        EventReturnerDTO dto = new EventReturnerDTO(id, username, title, description, location, startDate, endDate, images, participants, commentIds);

        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(location, dto.getLocation());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(images, dto.getImages());
        assertEquals(participants, dto.getParticipants());
        assertEquals(commentIds, dto.getComment_ids());
    }

    @Test
    public void testSettersAndGetters() {
        EventReturnerDTO dto = new EventReturnerDTO();

        Long id = 2L;
        String username = "newUser";
        String title = "Updated Event";
        String description = "Updated Description";
        String location = "Updated Location";
        long startDate = 1631234567890L;
        long endDate = 1631334567890L;
        List<String> images = Arrays.asList("image3.jpg", "image4.jpg");
        Set<String> participants = new HashSet<>(Arrays.asList("user3", "user4"));
        List<Long> commentIds = Arrays.asList(4L, 5L);

        dto.setId(id);
        dto.setUsername(username);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setLocation(location);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setImages(images);
        dto.setParticipants(participants);
        dto.setComment_ids(commentIds);

        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(location, dto.getLocation());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(images, dto.getImages());
        assertEquals(participants, dto.getParticipants());
        assertEquals(commentIds, dto.getComment_ids());
    }

    @Test
    public void testToString() {
        Long id = 1L;
        String username = "testUser";
        String title = "Test Event";
        String description = "Test Description";
        String location = "Test Location";
        long startDate = 1621234567890L;
        long endDate = 1621334567890L;
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        Set<String> participants = new HashSet<>(Arrays.asList("user1", "user2"));
        List<Long> commentIds = Arrays.asList(1L, 2L, 3L);

        EventReturnerDTO dto = new EventReturnerDTO(id, username, title, description, location, startDate, endDate, images, participants, commentIds);

        String expected = "EventReturnerDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", images=" + images +
                ", participants=" + participants +
                ", comment_ids=" + commentIds +
                '}';
        assertEquals(expected, dto.toString());
    }
}