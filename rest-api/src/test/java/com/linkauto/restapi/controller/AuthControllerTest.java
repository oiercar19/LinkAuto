package com.linkauto.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.linkauto.restapi.dto.CredencialesDTO;
import com.linkauto.restapi.dto.UserRegisterDTO;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

public class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    public void testRegisterSuccess() {
        UserRegisterDTO userDTO = new UserRegisterDTO("user1", "USER" , "User One", "profile.jpg", "user1@example.com", new ArrayList<>(), 123456L, "MALE", "Somewhere", "password123", "A description");
        User user = authController.parseUserRegisterDTOToUser(userDTO);
        
        when(authService.register(any(User.class))).thenReturn(true);

        ResponseEntity<Void> response = authController.register(userDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService, times(1)).register(any(User.class));
    }

    @Test
    public void testRegisterFailure() {
        UserRegisterDTO userDTO = new UserRegisterDTO("user2", "USER" , "User Two", "profile2.jpg", "user2@example.com", new ArrayList<>(), 987654L, "FEMALE", "Nowhere", "password456", "Another description");
        
        when(authService.register(any(User.class))).thenReturn(false);

        ResponseEntity<Void> response = authController.register(userDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(authService, times(1)).register(any(User.class));
    }

    @Test
    public void testLoginSuccess() {
        CredencialesDTO credenciales = new CredencialesDTO("user1", "password123");
        when(authService.login("user1", "password123")).thenReturn("valid-token-123");

        ResponseEntity<String> response = authController.login(credenciales);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("valid-token-123", response.getBody());
    }

    @Test
    public void testLoginFailure() {
        CredencialesDTO credenciales = new CredencialesDTO("user1", "wrongpassword");
        when(authService.login("user1", "wrongpassword")).thenReturn(null);

        ResponseEntity<String> response = authController.login(credenciales);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testLogoutSuccess() {
        String userToken = "valid-token-123";
        when(authService.logout(userToken)).thenReturn(true);

        ResponseEntity<Void> response = authController.logout(userToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).logout(userToken);
    }

    @Test
    public void testLogoutFailure() {
        String userToken = "invalid-token-456";
        when(authService.logout(userToken)).thenReturn(false);

        ResponseEntity<Void> response = authController.logout(userToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService, times(1)).logout(userToken);
    }
}

