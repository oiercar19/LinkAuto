package com.linkauto.client.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
}
