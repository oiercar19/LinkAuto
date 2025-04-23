package com.linkauto.restapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.linkauto.restapi.dto.CredencialesDTO;
import com.linkauto.restapi.dto.UserDTO;
import com.linkauto.restapi.dto.UserRegisterDTO;
import com.linkauto.restapi.dto.UserReturnerDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {
    
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testUserRegistersAndUpdatesProfile() {
        // 1. Registrar un nuevo usuario
        String username = "userTestUser_" + System.currentTimeMillis();  
        UserRegisterDTO user = new UserRegisterDTO(
                username,
                "Test User", 
                "avatar.jpg", 
                "user@test.com", 
                List.of("BMW M3", "Toyota Supra"),  // Lista simplificada
                946684800000L, 
                "MALE", 
                "Madrid", 
                "password123", 
                "This is my test profile"
        );
        
        ResponseEntity<Void> regResponse = testRestTemplate.postForEntity(
                "/auth/register", 
                user, 
                Void.class
        );
        assertEquals(HttpStatus.CREATED, regResponse.getStatusCode());
        
        // 2. Login
        CredencialesDTO creds = new CredencialesDTO(username, "password123");
        ResponseEntity<String> loginResponse = testRestTemplate.postForEntity(
                "/auth/login", 
                creds, 
                String.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = loginResponse.getBody();
        assertNotNull(token);
        
        // 3. Obtener perfil de usuario para verificar registro
        ResponseEntity<UserReturnerDTO> getUserResponse = testRestTemplate.getForEntity(
                "/api/user/" + username + "?userToken=" + token, 
                UserReturnerDTO.class
        );
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        UserReturnerDTO userProfile = getUserResponse.getBody();
        assertNotNull(userProfile);
        assertEquals(username, userProfile.getUsername());
        assertEquals("Test User", userProfile.getName());
        assertEquals("user@test.com", userProfile.getEmail());
        
        // 4. Actualizar perfil de usuario
        UserDTO updatedUserInfo = new UserDTO(
                "Updated Name",
                "new_avatar.jpg",
                "updated@test.com",
                List.of("Ford Mustang", "Porsche 911"),
                946684800000L,
                "MALE",
                "Barcelona",
                "password123",
                "My updated profile description"
                
        );
        
        // Usar el método correcto para actualizar el perfil
        ResponseEntity<Void> updateResponse = testRestTemplate.exchange(
                "/api/user?userToken=" + token,  
                HttpMethod.PUT,
                new HttpEntity<>(updatedUserInfo),
                Void.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 5. Verificar la actualización
        /*ResponseEntity<UserReturnerDTO> getUpdatedResponse = testRestTemplate.getForEntity(
                "/api/user/?userToken=" + token, 
                UserReturnerDTO.class
        );
        assertEquals(HttpStatus.OK, getUpdatedResponse.getStatusCode());
        UserReturnerDTO updatedProfile = getUpdatedResponse.getBody();
        assertNotNull(updatedProfile);
        assertEquals("Updated Name", updatedProfile.getName());
        assertEquals("updated@test.com", updatedProfile.getEmail());
        assertEquals("Barcelona", updatedProfile.getLocation());
        */
        // 6. Eliminar usuario
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                "/api/user/" + username + "?userToken=" + token,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        
        // 7. Verificar eliminación 
        /*ResponseEntity<UserReturnerDTO> checkDeletedResponse = testRestTemplate.getForEntity(
                "/api/user/" + username + "?userToken=" + token,
                UserReturnerDTO.class
        );
        assertEquals(HttpStatus.NOT_FOUND, checkDeletedResponse.getStatusCode()); */
    }
}