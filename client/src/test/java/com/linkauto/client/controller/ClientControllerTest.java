package com.linkauto.client.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties.RSocket.Client;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Comment;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
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
        User user = mock(User.class);

        String result = clientController.performRegister(user, redirectAttributes);

        verify(linkAutoServiceProxy).register(user);
        verify(redirectAttributes).addFlashAttribute("success", "Usuario registrado con éxito");
        assertEquals("redirect:/", result);
    }

    @Test
    public void testPerformRegister_Error() {
        User user = mock(User.class);

        doThrow(new RuntimeException("Error al registrar el usuario")).when(linkAutoServiceProxy).register(user);

        String result = clientController.performRegister(user, redirectAttributes);

        verify(linkAutoServiceProxy).register(user);
        verify(redirectAttributes).addFlashAttribute("error", "Error al registrar el usuario: Error al registrar el usuario");
        assertEquals("redirect:/register", result);
    }
 
        @Test
    public void testFeed_WithValidToken() {
        clientController.token = "validToken";
        clientController.username = "testUser";

        // Mocking data
        List<Post> posts = new ArrayList<>();
        Post post1 = mock(Post.class);
        Post post2 = mock(Post.class);
        posts.add(post1);
        posts.add(post2);

        User user = mock(User.class);
        when(linkAutoServiceProxy.getFeed()).thenReturn(posts);
        when(linkAutoServiceProxy.getUserProfile(clientController.token)).thenReturn(user);
        when(user.profilePicture()).thenReturn("profilePic.jpg");
        when(user.role()).thenReturn("USER");

        when(post1.username()).thenReturn("user1");
        when(post2.username()).thenReturn("user2");
        when(linkAutoServiceProxy.getUserByUsername("user1")).thenReturn(new User("user1", "USER", "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc"));
        when(linkAutoServiceProxy.getUserByUsername("user2")).thenReturn(new User("user2", "USER", "User Two", "pic2.jpg", "user2@example.com", null, 0, "Female", "Location2", "password", "desc"));

        List<User> followings = new ArrayList<>();
        followings.add(new User("user3", "USER", "User Three", "pic3.jpg", "user3@example.com", null, 0, "Male", "Location3", "password", "desc"));
        when(linkAutoServiceProxy.getUserFollowing("testUser")).thenReturn(followings);

        List<Comment> comments = new ArrayList<>();
        Comment comment = mock(Comment.class);
        comments.add(comment);
        when(linkAutoServiceProxy.getCommentsByPostId(anyLong())).thenReturn(comments);

        // Call the method
        String result = clientController.feed(model);

        // Verify interactions and attributes
        verify(model).addAttribute(eq("profilePictureByUsername"), any(Map.class));
        verify(model).addAttribute("posts", posts);
        verify(model).addAttribute("username", "testUser");
        verify(model).addAttribute("profilePicture", "profilePic.jpg");
        verify(model).addAttribute("role", "USER");
        verify(model).addAttribute(eq("followings"), any(List.class));
        verify(model).addAttribute(eq("commentsByPostId"), any(Map.class));

        assertEquals("feed", result);
    }

    @Test
    public void testFeed_WithInvalidToken() {
        clientController.token = null;

        // Call the method
        String result = clientController.feed(model);

        // Verify no interactions with the model
        verifyNoInteractions(model);

        // Verify redirection to login
        assertEquals("redirect:/", result);
    }

    @Test
    public void testShowUpdateProfile_WithValidToken() {
        clientController.token = "validToken";
        User user = mock(User.class);

        when(linkAutoServiceProxy.getUserProfile(clientController.token)).thenReturn(user);

        String result = clientController.showUpdateProfile(model);

        verify(model).addAttribute("user", user);
        assertEquals("editProfile", result);
    }

    @Test
    public void testShowUpdateProfile_WithInvalidToken() {
        clientController.token = null;

        String result = clientController.showUpdateProfile(model);

        verifyNoInteractions(model);
        assertEquals("redirect:/", result);
    }

    @Test
    public void testUpdateProfile_Success() {
        clientController.token = "validToken";
        User user = mock(User.class);

        String result = clientController.updateProfile(user, redirectAttributes);

        verify(linkAutoServiceProxy).updateProfile(clientController.token, user);
        verify(redirectAttributes).addFlashAttribute("success", "Perfil actualizado con éxito");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testUpdateProfile_Error() {
        clientController.token = "validToken";
        User user = mock(User.class);

        doThrow(new RuntimeException("Error al actualizar el perfil")).when(linkAutoServiceProxy).updateProfile(clientController.token, user);

        String result = clientController.updateProfile(user, redirectAttributes);

        verify(linkAutoServiceProxy).updateProfile(clientController.token, user);
        verify(redirectAttributes).addFlashAttribute("error", "Error al actualizar el perfil: Error al actualizar el perfil");
        assertEquals("redirect:/feed", result);
    }

        @Test
    public void testCreatePost_Success() {
        clientController.token = "validToken";
        PostCreator post = mock(PostCreator.class);

        String result = clientController.createPost(post, redirectAttributes);

        verify(linkAutoServiceProxy).createPost(clientController.token, post);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación creada con éxito");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testCreatePost_Error() {
        clientController.token = "validToken";
        PostCreator post = mock(PostCreator.class);

        doThrow(new RuntimeException("Error al crear la publicación")).when(linkAutoServiceProxy).createPost("validToken", post);

        String result = clientController.createPost(post, redirectAttributes);

        verify(linkAutoServiceProxy).createPost(clientController.token, post);
        verify(redirectAttributes).addFlashAttribute("error", "Error al crear la publicación: Error al crear la publicación");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testDeletePost_Success() {
        clientController.token = "validToken";
        Long postId = 1L;
        String redirectUrl = "/feed";

        String result = clientController.deletePost(postId, redirectUrl, redirectAttributes);

        verify(linkAutoServiceProxy).deletePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación eliminada con éxito");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testDeletePost_Error() {
        clientController.token = "validToken";
        Long postId = 1L;
        String redirectUrl = "/feed";

        doThrow(new RuntimeException("Error al eliminar la publicación")).when(linkAutoServiceProxy).deletePost("validToken", postId);

        String result = clientController.deletePost(postId, redirectUrl, redirectAttributes);

        verify(linkAutoServiceProxy).deletePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("error", "Error al eliminar la publicación: Error al eliminar la publicación");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testDeletePost_WithNullRedirectUrl() {
        clientController.token = "validToken";
        Long postId = 1L;

        String result = clientController.deletePost(postId, null, redirectAttributes);

        verify(linkAutoServiceProxy).deletePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación eliminada con éxito");
        assertEquals("redirect:/", result);
    }

    @Test
    public void testFollowUser_Success() {
        clientController.token = "validToken";
        String usernameToFollow = "testUser";
        String redirectUrl = "/feed";

        String result = clientController.followUser(usernameToFollow, redirectUrl, redirectAttributes);

        verify(linkAutoServiceProxy).followUser(clientController.token, usernameToFollow);
        verify(redirectAttributes).addFlashAttribute("success", "Siguiendo a " + usernameToFollow);
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testFollowUser_Error() {
        clientController.token = "validToken";
        String usernameToFollow = "testUser";
        String redirectUrl = "/feed";

        doThrow(new RuntimeException("Error al seguir al usuario")).when(linkAutoServiceProxy).followUser("validToken", usernameToFollow);

        String result = clientController.followUser(usernameToFollow, redirectUrl, redirectAttributes);

        verify(linkAutoServiceProxy).followUser(clientController.token, usernameToFollow);
        verify(redirectAttributes).addFlashAttribute("error", "Error al seguir al usuario: Error al seguir al usuario");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testFollowUser_WithNullRedirectUrl() {
        clientController.token = "validToken";
        String usernameToFollow = "testUser";

        String result = clientController.followUser(usernameToFollow, null, redirectAttributes);

        verify(linkAutoServiceProxy).followUser(clientController.token, usernameToFollow);
        verify(redirectAttributes).addFlashAttribute("success", "Siguiendo a " + usernameToFollow);
        assertEquals("redirect:/", result);
    }

    @Test
    public void testUnfollowUser_Success() {
        clientController.token = "validToken";
        String usernameToUnfollow = "testUser";
        String redirectUrl = "/feed";

        String result = clientController.unfollowUser(usernameToUnfollow, redirectUrl, redirectAttributes);

        verify(linkAutoServiceProxy).unfollowUser(clientController.token, usernameToUnfollow);
        verify(redirectAttributes).addFlashAttribute("success", "Dejado de seguir a " + usernameToUnfollow);
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testUnfollowUser_Error() {
        clientController.token = "validToken";
        String usernameToUnfollow = "testUser";
        String redirectUrl = "/feed";

        doThrow(new RuntimeException("Error al dejar de seguir al usuario")).when(linkAutoServiceProxy).unfollowUser("validToken", usernameToUnfollow);

        String result = clientController.unfollowUser(usernameToUnfollow, redirectUrl, redirectAttributes);

        verify(linkAutoServiceProxy).unfollowUser(clientController.token, usernameToUnfollow);
        verify(redirectAttributes).addFlashAttribute("error", "Error al dejar de seguir al usuario: Error al dejar de seguir al usuario");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testUnfollowUser_WithNullRedirectUrl() {
        clientController.token = "validToken";
        String usernameToUnfollow = "testUser";

        String result = clientController.unfollowUser(usernameToUnfollow, null, redirectAttributes);

        verify(linkAutoServiceProxy).unfollowUser(clientController.token, usernameToUnfollow);
        verify(redirectAttributes).addFlashAttribute("success", "Dejado de seguir a " + usernameToUnfollow);
        assertEquals("redirect:/", result);
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
