package com.linkauto.restapi.dto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class EventDTOTest {

    @Test
    public void testAllArgsConstructor() {
        String title = "Test Event";
        String description = "Test Description";
        String location = "Test Location";
        Long startDate = 1621234567890L;
        Long endDate = 1621334567890L;
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        
        EventDTO eventDTO = new EventDTO(title, description, location, startDate, endDate, images);

        assertEquals(title, eventDTO.getTitle());
        assertEquals(description, eventDTO.getDescription());
        assertEquals(location, eventDTO.getLocation());
        assertEquals(startDate, eventDTO.getStartDate());
        assertEquals(endDate, eventDTO.getEndDate());
        assertEquals(images, eventDTO.getImages());
    }

    @Test
    public void testSettersAndGetters() {
        EventDTO eventDTO = new EventDTO(null, null, null, null, null, null);

        String title = "Updated Event";
        String description = "Updated Description";
        String location = "Updated Location";
        Long startDate = 1631234567890L;
        Long endDate = 1631334567890L;
        List<String> images = Arrays.asList("image3.jpg", "image4.jpg");

        eventDTO.setTitle(title);
        eventDTO.setDescription(description);
        eventDTO.setLocation(location);
        eventDTO.setStartDate(startDate);
        eventDTO.setEndDate(endDate);
        eventDTO.setImages(images);

        assertEquals(title, eventDTO.getTitle());
        assertEquals(description, eventDTO.getDescription());
        assertEquals(location, eventDTO.getLocation());
        assertEquals(startDate, eventDTO.getStartDate());
        assertEquals(endDate, eventDTO.getEndDate());
        assertEquals(images, eventDTO.getImages());
    }
}