package com.linkauto.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class UserDTOTest {

    @Test
    public void testAllArgsConstructor() {
        String name = "John Doe";
        String profilePicture = "profile.jpg";
        String email = "john@example.com";
        List<String> cars = Arrays.asList("BMW", "Audi");
        long birthDate = 946684800L; // Año 2000 en timestamp
        String gender = "Male";
        String location = "New York";
        String password = "securePassword";
        String description = "Enthusiast car lover";

        UserDTO user = new UserDTO(name, profilePicture, email, cars, birthDate, gender, location, password, description);

        assertEquals(name, user.getName());
        assertEquals(profilePicture, user.getProfilePicture());
        assertEquals(email, user.getEmail());
        assertEquals(cars, user.getCars());
        assertEquals(birthDate, user.getBirthDate());
        assertEquals(gender, user.getGender());
        assertEquals(location, user.getLocation());
        assertEquals(password, user.getPassword());
        assertEquals(description, user.getDescription());
    }

    @Test
    public void testSettersAndGetters() {
        UserDTO user = new UserDTO("", "", "", null, 0L, "", "", "", "");

        String name = "Jane Doe";
        String profilePicture = "avatar.png";
        String email = "jane@example.com";
        List<String> cars = Arrays.asList("Tesla", "Ford");
        long birthDate = 978307200L; // Año 2001
        String gender = "Female";
        String location = "Los Angeles";
        String password = "anotherPassword";
        String description = "Electric car enthusiast";

        user.setName(name);
        user.setProfilePicture(profilePicture);
        user.setEmail(email);
        user.setCars(cars);
        user.setBirthDate(birthDate);
        user.setGender(gender);
        user.setLocation(location);
        user.setPassword(password);
        user.setDescription(description);

        assertEquals(name, user.getName());
        assertEquals(profilePicture, user.getProfilePicture());
        assertEquals(email, user.getEmail());
        assertEquals(cars, user.getCars());
        assertEquals(birthDate, user.getBirthDate());
        assertEquals(gender, user.getGender());
        assertEquals(location, user.getLocation());
        assertEquals(password, user.getPassword());
        assertEquals(description, user.getDescription());
    }
}
