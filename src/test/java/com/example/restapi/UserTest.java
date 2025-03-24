package com.example.restapi;

import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;
import com.example.restapi.service.LinkAutoService;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserTest {
    private UserRepository userRepository;
    private LinkAutoService linkAutoService;

    @Test
    void testGetAllUsers() {
        User user1 = new User("user1", "Name1", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "Description1");
        User user2 = new User("user2", "Name2", "profile2.jpg", "user2@example.com", Arrays.asList("Car2"), 1234567890L, User.Gender.FEMALE, "Location2", "password2", "Description2");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = linkAutoService.getAllUsers();
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserByUsername() {
        User user = new User("user1", "Name1", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "Description1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        Optional<User> foundUser = linkAutoService.getUserByUsername("user1");
        assertTrue(foundUser.isPresent());
        assertEquals("user1", foundUser.get().getUsername());
        verify(userRepository, times(1)).findById("user1");
    }

    @Test
    void testCreateUser() {
        User user = new User("user1", "Name1", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "Description1");
        when(userRepository.save(user)).thenReturn(user);

        User createdUser = linkAutoService.createUser(user);
        assertEquals("user1", createdUser.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser() {
        User user = new User("user1", "Name1", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "Description1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User userDetails = new User("user1", "UpdatedName", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "UpdatedDescription");
        User updatedUser = linkAutoService.updateUser("user1", userDetails);

        assertEquals("UpdatedName", updatedUser.getName());
        assertEquals("UpdatedDescription", updatedUser.getDescription());
        verify(userRepository, times(1)).findById("user1");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser() {
        User user = new User("user1", "Name1", "profile1.jpg", "user1@example.com", Arrays.asList("Car1"), 1234567890L, User.Gender.MALE, "Location1", "password1", "Description1");
        when(userRepository.existsById("user1")).thenReturn(true);

        linkAutoService.deleteUser("user1");
        verify(userRepository, times(1)).deleteById("user1");
    }
}
