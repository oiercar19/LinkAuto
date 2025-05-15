package com.linkauto.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.Role;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.repository.UserRepository;

public class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    private User user;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        authService = new AuthService(userRepository);
    }

    @BeforeEach
    public void setUpUser() {
        user = new User("testUser", "name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Test
    public void testRegister_UserAlreadyExists() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        boolean result = authService.register(user);
        assertFalse(result);
    }

    @Test
    public void testRegister_NewUser() {
        User user = new User("newUser", "name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.register(user);
        assertTrue(result);
        verify(userRepository).save(user);
    }

    @Test
    public void testLogin_Success() {
        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));

        String token = authService.login("testUser", "password");

        assertNotNull(token);
        assertTrue(authService.isTokenValid(token));
        assertEquals(user, authService.getUserByToken(token));
    }

    @Test
    public void testLogin_InvalidPassword() {
        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));

        String token = authService.login("testUser", "wrongPassword");

        assertNull(token);
    }

    @Test
    public void testLogin_UserNotFound() {
        when(userRepository.findById("nonExistent")).thenReturn(Optional.empty());

        String token = authService.login("nonExistent", "password");

        assertNull(token);
    }

    @Test
    public void testLogout() {
        String token = authService.login("testUser", "password");
        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));

        // Valid login
        token = authService.login("testUser", "password");
        assertNotNull(token);
        assertTrue(authService.isTokenValid(token));

        // Logout
        boolean result = authService.logout(token);
        assertTrue(result);
        assertFalse(authService.isTokenValid(token));
    }

    @Test
        public void testLogout_TokenNotFound() {
        String fakeToken = "nonExistentToken";

        boolean result = authService.logout(fakeToken);

        assertFalse(result);
    }

    @Test
    public void testUpdateUser() {
        String token = authService.login("testUser", "password");

        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.updateUser(user, token);
        assertTrue(result);
        assertEquals(user, authService.getUserByToken(token));
        verify(userRepository).save(user);
    }

    @Test
    public void testDeleteUser_Success() {
        User user = new User("testUser", "name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Post post = new Post(1L, user, "Post message", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        user.addPost(post);
        User user2 = new User("testUser2", "name2", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        user.addFollower(user2);
        user2.addFollowing(user);
        user.addFollowing(user2);
        user2.addFollower(user);

        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));
        String token = authService.login("testUser", "password");

        doNothing().when(userRepository).delete(user);

        boolean result = authService.deleteUser(user, token);
        assertTrue(result);
        assertFalse(authService.isTokenValid(token));
        verify(userRepository).delete(user);
    }

    @Test
    public void testDeleteUser_Exception() {
        String token = authService.login("testUser", "password");

        doThrow(new RuntimeException("Error de base de datos")).when(userRepository).delete(user);

        boolean result = authService.deleteUser(user, token);
        assertFalse(result);

        boolean result2 = authService.deleteUser(null, null);
        assertFalse(result2);
    }

    @Test
    public void testDeleteUser_CatchException() {
        User user = new User("testUser", "name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));

        String token = authService.login("testUser", "password");

        doThrow(new RuntimeException("Error de base de datos")).when(userRepository).delete(any(User.class));

        boolean result = authService.deleteUser(user, token);
        assertFalse(result);
    }

    @Test
    public void testDeleteUser_AdminCanDeleteOtherUser() {
        User user = new User("testUser", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        User adminUser = new User("adminUser", "Admin Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));
        when(userRepository.findById("adminUser")).thenReturn(Optional.of(adminUser));

        String tokenAdmin = authService.login("adminUser", "password");

        doNothing().when(userRepository).delete(user);

        boolean result = authService.deleteUser(user, tokenAdmin);

        assertTrue(result);
        assertFalse(authService.isTokenValid(tokenAdmin)); 
}

@Test
    public void testBanUser_Success() {
        // Datos de prueba
        String username = "testUser";
        boolean banStatus = true;

        // Crear un usuario de prueba
        User user = new User();
        user.setUsername(username);
        user.setBanned(false);

        // Mock del repositorio
        when(userRepository.findById(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Llamar al método
        boolean result = authService.banUser(username, banStatus);

        // Verificar el resultado
        assertTrue(result);
        assertTrue(user.isBanned());
        verify(userRepository).findById(username);
        verify(userRepository).save(user);
    }

    @Test
    public void testBanUser_UserNotFound() {
        // Datos de prueba
        String username = "nonExistentUser";
        boolean banStatus = true;

        // Mock del repositorio
        when(userRepository.findById(username)).thenReturn(Optional.empty());

        // Llamar al método
        boolean result = authService.banUser(username, banStatus);

        // Verificar el resultado
        assertFalse(result);
        verify(userRepository).findById(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testBanUser_ErrorWhileSaving() {
        // Datos de prueba
        String username = "testUser";
        boolean banStatus = true;

        // Crear un usuario de prueba
        User user = new User();
        user.setUsername(username);
        user.setBanned(false);

        // Mock del repositorio
        when(userRepository.findById(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Error al guardar el usuario"));

        // Llamar al método
        boolean result = authService.banUser(username, banStatus);

        // Verificar el resultado
        assertFalse(result);
        verify(userRepository).findById(username);
        verify(userRepository).save(user); // Verificar que se intentó guardar el usuario
    }

    @Test
    public void testChangeRole() {
        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.changeRole(user, Role.ADMIN);

        assertTrue(result);
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    public void testGetUserByUsername_UserExists() {
        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));

        User result = authService.getUserByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    public void testGetUserByUsername_UserNotFound() {
        when(userRepository.findById("nonExistent")).thenReturn(Optional.empty());

        User result = authService.getUserByUsername("nonExistent");

        assertNull(result);
    }

    @Test
    public void testIsTokenValid_False() {
        String invalidToken = "fakeToken";
        assertFalse(authService.isTokenValid(invalidToken));
    }

    @Test
    public void testGetUserByToken_Null() {
        String invalidToken = "nonExistentToken";
        assertNull(authService.getUserByToken(invalidToken));
    }


}
