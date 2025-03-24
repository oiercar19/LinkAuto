package com.example.restapi;

import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class RestApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

	@BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user1 = new User("user1", "Name1", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "Description1");
        User user2 = new User("user2", "Name2", "profile2.jpg", "user2@example.com", Arrays.asList("Car2"), 1234567890L, User.Gender.FEMALE, "Location2", "password2", "Description2");
        userRepository.saveAll(Arrays.asList(user1, user2));
    }

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetUserByUsername() throws Exception {
        mockMvc.perform(get("/api/users/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

	@Test
    void testCreateUser() throws Exception {
        User user = new User("user3", "Name3", "profile3.jpg", "user3@example.com", Arrays.asList("Car3"), 1234567890L, User.Gender.MALE, "Location3", "password3", "Description3");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user3\",\"name\":\"Name3\",\"profilePicture\":\"profile3.jpg\",\"email\":\"user3@example.com\",\"cars\":[\"Car3\"],\"birthDate\":1234567890,\"gender\":\"MALE\",\"location\":\"Location3\",\"password\":\"password3\",\"description\":\"Description3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user3"));
    }

    @Test
    void testUpdateUser() throws Exception {
        mockMvc.perform(put("/api/users/user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user1\",\"name\":\"UpdatedName\",\"profilePicture\":\"profile1.jpg\",\"email\":\"user1@example.com\",\"cars\":[\"Car1\"],\"birthDate\":1234567890,\"gender\":\"MALE\",\"location\":\"Location1\",\"password\":\"password1\",\"description\":\"UpdatedDescription\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.description").value("UpdatedDescription"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/user1"))
                .andExpect(status().isNoContent());
    }

	@Test
	void contextLoads() {
	}

}
