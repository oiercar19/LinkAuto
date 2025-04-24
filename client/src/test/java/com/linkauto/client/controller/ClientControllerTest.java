package com.linkauto.client.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.User;
import com.linkauto.client.service.ClientServiceProxy;

public class ClientControllerTest {

    private ClientController clientController;
    private ClientServiceProxy linkAutoServiceProxy;
    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    public void setUp() {
        linkAutoServiceProxy = mock(ClientServiceProxy.class);
        clientController = new ClientController();
        clientController.linkAutoServiceProxy = linkAutoServiceProxy;
        model = mock(Model.class);
        redirectAttributes = mock(RedirectAttributes.class);
    }

        @Test
    public void testLogin_Success() {
        String username = "testUser";
        String password = "testPassword";
        String token = "validToken";
        User user = mock(User.class);

        when(linkAutoServiceProxy.login(new Credentials(username, password))).thenReturn(token);
        when(linkAutoServiceProxy.getUserProfile(token)).thenReturn(user);
        when(user.username()).thenReturn(username);

        String result = clientController.login(username, password, redirectAttributes, model);

        assertEquals("redirect:/feed", result);
        assertEquals(token, clientController.token);
        assertEquals(username, clientController.username);
    }

    @Test
    public void testLogin_Failure() {
        String username = "testUser";
        String password = "wrongPassword";

        when(linkAutoServiceProxy.login(new Credentials(username, password))).thenThrow(new RuntimeException("Credenciales incorrectas"));

        String result = clientController.login(username, password, redirectAttributes, model);

        verify(redirectAttributes).addFlashAttribute("error", "Credenciales incorrectas");
        assertEquals("redirect:/", result);
    }

    @Test
    public void testLogout() {
        clientController.token = "validToken";
        clientController.username = "testUser";

        String result = clientController.logout(redirectAttributes);

        verify(linkAutoServiceProxy).logout("validToken");
        assertEquals("redirect:/", result);
        assertEquals(null, clientController.token);
        assertEquals(null, clientController.username);
    }


    @Test
    public void testShowRegister_WithRedirectUrl() {
        String redirectUrl = "/feed";

        String result = clientController.showRegister(redirectUrl, model);

        verify(model).addAttribute("redirectUrl", redirectUrl);
        assertEquals("register", result);
    }

    @Test
    public void testShowRegister_WithoutRedirectUrl() {
        String result = clientController.showRegister(null, model);

        verify(model).addAttribute("redirectUrl", null);
        assertEquals("register", result);
    }

    @Test
    public void testPerformRegister_Success() {
        User user = new User(
            "testUser",          // username
            "USER",             // role
            "Test Name",         // name
            "profilePic.jpg",    // profilePicture
            "test@example.com",  // email
            List.of("Car1", "Car2"), // cars
            123456789L,          // birthDate
            "MALE",              // gender
            "Test Location",     // location
            "password123",       // password
            "Test description"   // description
        );

        String result = clientController.performRegister(user, redirectAttributes);

        verify(linkAutoServiceProxy).register(user);
        verify(redirectAttributes).addFlashAttribute("success", "Usuario registrado con éxito");
        assertEquals("redirect:/", result);
    }

    @Test
    public void testPerformRegister_Error() {
        User user = new User(
            "testUser",          // username
            "USER",             // role
            "Test Name",         // name
            "profilePic.jpg",    // profilePicture
            "test@example.com",  // email
            List.of("Car1", "Car2"), // cars
            123456789L,          // birthDate
            "MALE",              // gender
            "Test Location",     // location
            "password123",       // password
            "Test description"   // description
        );

        doThrow(new RuntimeException("Error al registrar el usuario")).when(linkAutoServiceProxy).register(user);

        String result = clientController.performRegister(user, redirectAttributes);

        verify(linkAutoServiceProxy).register(user);
        verify(redirectAttributes).addFlashAttribute("error", "Error al registrar el usuario: Error al registrar el usuario");
        assertEquals("redirect:/register", result);
    }
 
    @Test
    public void testAdminPanel_TokenNull() {
        clientController.token = null;

        String result = clientController.adminPanel(model, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para acceder al panel de administrador.");
        assertEquals("redirect:/", result);
    }

    @Test
    public void testAdminPanel_UserNotAdmin() {
        clientController.token = "validToken";
        User user = mock(User.class);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);
        when(user.role()).thenReturn("USER");

        String result = clientController.adminPanel(model, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "No tienes permisos para acceder al panel de administrador.");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testAdminPanel_UserAdmin() {
        clientController.token = "validToken";
        User user = mock(User.class);
        List<User> users = new ArrayList<>();
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);
        when(user.role()).thenReturn("ADMIN");
        when(linkAutoServiceProxy.getAllUsers()).thenReturn(users);

        String result = clientController.adminPanel(model, redirectAttributes);

        verify(model).addAttribute("users", users);
        assertEquals("adminPanel", result);
    }

    @Test
    public void testDeleteUser_Success() {
        clientController.token = "validToken";
        String usernameToDelete = "testUser";

        String result = clientController.deleteUser(usernameToDelete, redirectAttributes);

        verify(linkAutoServiceProxy).deleteUser(clientController.token, usernameToDelete);
        verify(redirectAttributes).addFlashAttribute("success", "Usuario " + usernameToDelete + " eliminado con éxito.");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testDeleteUser_Error() {
        clientController.token = "validToken";
        String usernameToDelete = "testUser";
        doThrow(new RuntimeException("Error al eliminar el usuario")).when(linkAutoServiceProxy).deleteUser("validToken", usernameToDelete);

        String result = clientController.deleteUser(usernameToDelete, redirectAttributes);

        verify(linkAutoServiceProxy).deleteUser(clientController.token, usernameToDelete);
        verify(redirectAttributes).addFlashAttribute("error", "Error al eliminar el usuario: Error al eliminar el usuario");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testPromoteToAdmin_Success() {
        clientController.token = "validToken";
        String usernameToPromote = "testUser";

        String result = clientController.promoteToAdmin(usernameToPromote, redirectAttributes);

        verify(linkAutoServiceProxy).promoteToAdmin(clientController.token, usernameToPromote);
        verify(redirectAttributes).addFlashAttribute("success", "Usuario " + usernameToPromote + " promovido a administrador con éxito.");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testPromoteToAdmin_Error() {
        clientController.token = "validToken";
        String usernameToPromote = "testUser";
        doThrow(new RuntimeException("Error al promover al usuario")).when(linkAutoServiceProxy).promoteToAdmin("validToken", usernameToPromote);

        String result = clientController.promoteToAdmin(usernameToPromote, redirectAttributes);

        verify(linkAutoServiceProxy).promoteToAdmin(clientController.token, usernameToPromote);
        verify(redirectAttributes).addFlashAttribute("error", "Error al promover al usuario: Error al promover al usuario");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testDemoteToUser_Success() {
        clientController.token = "validToken";
        String usernameToDemote = "testAdmin";

        String result = clientController.demoteToUser(usernameToDemote, redirectAttributes);

        verify(linkAutoServiceProxy).promoteToAdmin(clientController.token, usernameToDemote);
        verify(redirectAttributes).addFlashAttribute("success", "Administrador " + usernameToDemote + " degradado con éxito.");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testDemoteToUser_Error() {
        clientController.token = "validToken";
        String usernameToDemote = "testAdmin";
        doThrow(new RuntimeException("Error al degradar al administrador")).when(linkAutoServiceProxy).promoteToAdmin("validToken", usernameToDemote);

        String result = clientController.demoteToUser(usernameToDemote, redirectAttributes);

        verify(linkAutoServiceProxy).promoteToAdmin(clientController.token, usernameToDemote);
        verify(redirectAttributes).addFlashAttribute("error", "Error al degradar al administrador: Error al degradar al administrador");
        assertEquals("redirect:/adminPanel", result);
    }
}
