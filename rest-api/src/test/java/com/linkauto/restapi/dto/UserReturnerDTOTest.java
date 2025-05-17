package com.linkauto.restapi.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class UserReturnerDTOTest {

    @Test
    public void testConstructorAndGetters() {
        List<String> cars = Arrays.asList("Car1", "Car2");
        List<PostReturnerDTO> posts = new ArrayList<>();
        posts.add(new PostReturnerDTO());
        posts.add(new PostReturnerDTO());
        List<PostReturnerDTO> savedPosts = new ArrayList<>();
        savedPosts.add(new PostReturnerDTO());
        savedPosts.add(new PostReturnerDTO());

        UserReturnerDTO user = new UserReturnerDTO(
            "username123",
            "USER",
            "John Doe",
            "profilePic.jpg",
            "john.doe@example.com",
            cars,
            123456789L,
            "Male",
            "New York",
            "password123",
            "This is a description.",
            posts,
            false
            savedPosts
        );

        assertEquals("username123", user.getUsername());
        assertEquals("USER", user.getRole());
        assertEquals("John Doe", user.getName());
        assertEquals("profilePic.jpg", user.getProfilePicture());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals(cars, user.getCars());
        assertEquals(123456789L, user.getBirthDate());
        assertEquals("Male", user.getGender());
        assertEquals("New York", user.getLocation());
        assertEquals("password123", user.getPassword());
        assertEquals("This is a description.", user.getDescription());
        assertEquals(posts, user.getPosts());
        assertEquals(savedPosts, user.getSavedPost());
    }

    @Test
    public void testSetters() {
        UserReturnerDTO user = new UserReturnerDTO(
            "username123",
            "USER",
            "John Doe",
            "profilePic.jpg",
            "john.doe@example.com",
            new ArrayList<>(),
            123456789L,
            "Male",
            "New York",
            "password123",
            "This is a description.",
            new ArrayList<>(),
            false
            new ArrayList<>()
        );

        user.setUsername("newUsername");
        user.setRole("ADMIN");
        user.setName("Jane Doe");
        user.setProfilePicture("newProfilePic.jpg");
        user.setEmail("jane.doe@example.com");
        user.setCars(Arrays.asList("Car3", "Car4"));
        user.setBirthDate(987654321L);
        user.setGender("Female");
        user.setLocation("Los Angeles");
        user.setPassword("newPassword123");
        user.setDescription("Updated description.");

        assertEquals("newUsername", user.getUsername());
        assertEquals("ADMIN", user.getRole());
        assertEquals("Jane Doe", user.getName());
        assertEquals("newProfilePic.jpg", user.getProfilePicture());
        assertEquals("jane.doe@example.com", user.getEmail());
        assertEquals(Arrays.asList("Car3", "Car4"), user.getCars());
        assertEquals(987654321L, user.getBirthDate());
        assertEquals("Female", user.getGender());
        assertEquals("Los Angeles", user.getLocation());
        assertEquals("newPassword123", user.getPassword());
        assertEquals("Updated description.", user.getDescription());
    }

    @Test
    public void testAddPost() {
        UserReturnerDTO user = new UserReturnerDTO(
            "username123",
            "USER",
            "John Doe",
            "profilePic.jpg",
            "john.doe@example.com",
            new ArrayList<>(),
            123456789L,
            "Male",
            "New York",
            "password123",
            "This is a description.",
            new ArrayList<>(),
            false
            new ArrayList<>()
        );

        PostReturnerDTO post = new PostReturnerDTO();
        user.addPost(post);

        assertEquals(1, user.getPosts().size());
        assertTrue(user.getPosts().contains(post));
    }
}