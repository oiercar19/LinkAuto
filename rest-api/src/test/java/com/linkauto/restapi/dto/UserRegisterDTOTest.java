package com.linkauto.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class UserRegisterDTOTest {

    @Test
    public void testAllArgsConstructor() {
        String username = "testUser";
        String name = "Test Name";
        String profilePicture = "profile.jpg";
        String email = "test@example.com";
        List<String> cars = Arrays.asList("Car1", "Car2");
        long birthDate = 123456789L;
        String gender = "Male";
        String location = "Test City";
        String password = "securePassword";
        String description = "This is a test user.";

        UserRegisterDTO dto = new UserRegisterDTO(username, name, profilePicture, email, cars, birthDate, gender, location, password, description);

        assertEquals(username, dto.getUsername());
        assertEquals("USER", dto.getRole());
        assertEquals(name, dto.getName());
        assertEquals(profilePicture, dto.getProfilePicture());
        assertEquals(email, dto.getEmail());
        assertEquals(cars, dto.getCars());
        assertEquals(birthDate, dto.getBirthDate());
        assertEquals(gender, dto.getGender());
        assertEquals(location, dto.getLocation());
        assertEquals(password, dto.getPassword());
        assertEquals(description, dto.getDescription());
    }

    @Test
    public void testSettersAndGetters() {
        UserRegisterDTO dto = new UserRegisterDTO(null,  null, null, null, null, 0, null, null, null, null);

        String username = "updatedUser";
        String role = "ADMIN";
        String name = "Updated Name";
        String profilePicture = "updated.jpg";
        String email = "updated@example.com";
        List<String> cars = Arrays.asList("CarX", "CarY");
        long birthDate = 987654321L;
        String gender = "Female";
        String location = "Updated City";
        String password = "newPassword";
        String description = "Updated description.";

        dto.setUsername(username);
        dto.setRole(role);
        dto.setName(name);
        dto.setProfilePicture(profilePicture);
        dto.setEmail(email);
        dto.setCars(cars);
        dto.setBirthDate(birthDate);
        dto.setGender(gender);
        dto.setLocation(location);
        dto.setPassword(password);
        dto.setDescription(description);

        assertEquals(username, dto.getUsername());
        assertEquals(role, dto.getRole());
        assertEquals(name, dto.getName());
        assertEquals(profilePicture, dto.getProfilePicture());
        assertEquals(email, dto.getEmail());
        assertEquals(cars, dto.getCars());
        assertEquals(birthDate, dto.getBirthDate());
        assertEquals(gender, dto.getGender());
        assertEquals(location, dto.getLocation());
        assertEquals(password, dto.getPassword());
        assertEquals(description, dto.getDescription());
    }
}
