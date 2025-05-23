package com.linkauto.client.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linkauto.client.data.Comment;
import com.linkauto.client.data.CommentCreator;
import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
import com.linkauto.client.data.UpdateUser;
import com.linkauto.client.data.User;
import com.linkauto.client.data.Event;
import com.linkauto.client.data.EventCreator;
import com.linkauto.client.service.ClientServiceProxy;

import jakarta.servlet.http.HttpServletRequest;

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
        String token = "validToken";
        User user = new User("user1", "USER", false ,"User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);

        when(linkAutoServiceProxy.login(new Credentials(user.username(), user.password()))).thenReturn(token);
        when(linkAutoServiceProxy.getUserProfile(token)).thenReturn(user);

        String result = clientController.login(user.username(), user.password(), redirectAttributes, model);

        assertEquals("redirect:/feed", result);
        assertEquals(token, clientController.token);
        assertEquals(user.username(), clientController.username);
    }
    @Test
    public void testLogin_BannedUser() {
        String token = "validToken";
        User user = new User("user1", "USER", true, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);

        when(linkAutoServiceProxy.login(new Credentials(user.username(), user.password()))).thenReturn(token);
        when(linkAutoServiceProxy.getUserProfile(token)).thenReturn(user);

        String result = clientController.login(user.username(), user.password(), redirectAttributes, model);

        verify(redirectAttributes).addFlashAttribute("error", "Tu cuenta está baneada. No puedes acceder a la plataforma.");
        assertEquals("banned", result);
    }

    @Test
    public void testLogin_Failure() {
        String username = "testUser";
        String password = "wrongPassword";

        when(linkAutoServiceProxy.login(new Credentials(username, password))).thenThrow(new RuntimeException("Credenciales incorrectas"));

        String result = clientController.login(username, password, redirectAttributes, model);

        verify(redirectAttributes).addFlashAttribute("error", "Credenciales incorrectas o error al iniciar sesión.");
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
        User user = new User("user1", "USER", false, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);

        doNothing().when(linkAutoServiceProxy).register(user);
        String result = clientController.performRegister(user, redirectAttributes);

        verify(linkAutoServiceProxy).register(user);
        verify(redirectAttributes).addFlashAttribute("success", "Usuario registrado con éxito");
        assertEquals("redirect:/", result);
    }

    @Test
    public void testPerformRegister_Error() {
        User user = new User("user1", "USER", false, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);


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
        Post post1 = new Post(1L, "user1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(2L, "user2", "content2", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post3 = new Post(3L, "user2", "content2", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        User user = new User("user1", "USER", false, "User One", "profilePic.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);
    
        when(linkAutoServiceProxy.getFeed()).thenReturn(posts);
        when(linkAutoServiceProxy.getUserProfile(clientController.token)).thenReturn(user);

        when(linkAutoServiceProxy.getUserByUsername("user1")).thenReturn(new User("user1", "USER", false, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false));
        when(linkAutoServiceProxy.getUserByUsername("user2")).thenReturn(new User("user2", "USER", false, "User Two", "pic2.jpg", "user2@example.com", null, 0, "Female", "Location2", "password", "desc", new HashSet<>(), false));

        List<User> followings = new ArrayList<>();
        followings.add(new User("user3", "USER",false ,"User Three", "pic3.jpg", "user3@example.com", null, 0, "Male", "Location3", "password", "desc", new HashSet<>(), false));

        when(linkAutoServiceProxy.getUserFollowing("testUser")).thenReturn(followings);

        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment(1L, "texto", "testuser", 1L, 43456L);
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
        
        User user = new User("user1", "USER",false , "User One", "profilePic.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);

        when(linkAutoServiceProxy.getUserByUsername("user1")).thenReturn(user);

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

        UpdateUser user = new UpdateUser("user1", "User One", "profilePic.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc");

        doNothing().when(linkAutoServiceProxy).updateProfile("validToken", user);
        String result = clientController.updateProfile(user, redirectAttributes);

        verify(linkAutoServiceProxy).updateProfile(clientController.token, user);
        verify(redirectAttributes).addFlashAttribute("success", "Perfil actualizado con éxito");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testUpdateProfile_Error() {
        clientController.token = "validToken";

        UpdateUser user = new UpdateUser("user1", "User One", "profilePic.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc");

        doThrow(new RuntimeException("Error al actualizar el perfil")).when(linkAutoServiceProxy).updateProfile(clientController.token, user);

        clientController.updateProfile(user, redirectAttributes);
        verify(linkAutoServiceProxy).updateProfile(clientController.token, user);
        verify(redirectAttributes).addFlashAttribute("error", "Error al actualizar el perfil: Error al actualizar el perfil");
    }
    @Test
    public void testCreatePost_Success() {
        clientController.token = "validToken";
        PostCreator post = new PostCreator("Test post content", new ArrayList<>());

        doNothing().when(linkAutoServiceProxy).createPost("validToken", post);
        String result = clientController.createPost(post, redirectAttributes);

        verify(linkAutoServiceProxy).createPost(clientController.token, post);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación creada con éxito");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testCreatePost_Error() {
        clientController.token = "validToken";
        PostCreator post = new PostCreator("Test post content", new ArrayList<>());

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
        clientController.username = "commonUser";

        User user = new User("commonUser", "USER", false, "Commenter Name", "commenter.jpg", "email", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false);
        
        when(linkAutoServiceProxy.getUserByUsername("commonUser")).thenReturn(user);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);

        String result = clientController.adminPanel(model, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "No tienes permisos para acceder al panel de administrador.");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testAdminPanel_UserAdmin() {
        clientController.token = "validToken";
        clientController.username = "adminUser";

        User user = new User("adminUser", "ADMIN", false, "Commenter Name", "commenter.jpg", "email", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false);
        List<User> users = new ArrayList<>();
        when(linkAutoServiceProxy.getUserByUsername("adminUser")).thenReturn(user);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);
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
    public void testBanUser_Success() {
        // Datos de prueba
        String usernameToBan = "user1";
        boolean banStatus = true; // Baneando al usuario
        clientController.token = "validToken";

        // Mock del servicio
        doNothing().when(linkAutoServiceProxy).banUser(clientController.token, usernameToBan, banStatus);
        when(linkAutoServiceProxy.getAllUsers()).thenReturn(List.of(
            new User("user1", "USER", true, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false),
            new User("user2", "USER", false, "User Two", "pic2.jpg", "user2@example.com", null, 0, "Female", "Location2", "password", "desc", new HashSet<>(), false)
        ));

        // Ejecutar el método
        String result = clientController.banUser(usernameToBan, banStatus, redirectAttributes, model);

        // Verificaciones
        verify(linkAutoServiceProxy).banUser(clientController.token, usernameToBan, banStatus);
        verify(linkAutoServiceProxy).getAllUsers();
        verify(redirectAttributes).addFlashAttribute("success", "Usuario " + usernameToBan + " baneado con éxito.");
        verify(model).addAttribute(eq("users"), anyList());
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testBanUser_Error() {
        // Datos de prueba
        String usernameToBan = "user1";
        boolean banStatus = true; // Baneando al usuario
        clientController.token = "validToken";

        // Mock del servicio para lanzar una excepción
        doThrow(new RuntimeException("Error al banear el usuario")).when(linkAutoServiceProxy).banUser(clientController.token, usernameToBan, banStatus);

        // Ejecutar el método
        String result = clientController.banUser(usernameToBan, banStatus, redirectAttributes, model);

        // Verificaciones
        verify(linkAutoServiceProxy).banUser(clientController.token, usernameToBan, banStatus);
        verify(redirectAttributes).addFlashAttribute("error", "Error al actualizar el estado de baneo del usuario: Error al banear el usuario");
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

        verify(linkAutoServiceProxy).demoteToUser(clientController.token, usernameToDemote);
        verify(redirectAttributes).addFlashAttribute("success", "Administrador " + usernameToDemote + " degradado con éxito.");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testDemoteToUser_Error() {
        clientController.token = "validToken";
        String usernameToDemote = "testAdmin";
        doThrow(new RuntimeException("Error al degradar al administrador")).when(linkAutoServiceProxy).demoteToUser("validToken", usernameToDemote);

        String result = clientController.demoteToUser(usernameToDemote, redirectAttributes);

        verify(linkAutoServiceProxy).demoteToUser(clientController.token, usernameToDemote);
        verify(redirectAttributes).addFlashAttribute("error", "Error al degradar al administrador: Error al degradar al administrador");
        assertEquals("redirect:/adminPanel", result);
    }

    @Test
    public void testHome() {
        String result = clientController.home();
        
        assertEquals("index", result);
    }
    
    @Test
    public void testAddAttributes() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        
        // Mock the static method in ServletUriComponentsBuilder
        try (MockedStatic<ServletUriComponentsBuilder> mockBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder mockUriBuilder = mock(ServletUriComponentsBuilder.class);
            mockBuilder.when(() -> ServletUriComponentsBuilder.fromRequestUri(any(HttpServletRequest.class)))
                    .thenReturn(mockUriBuilder);
            when(mockUriBuilder.toUriString()).thenReturn("http://test.com/feed");
            
            clientController.addAttributes(model, request);
            
            verify(model).addAttribute("currentUrl", "http://test.com/feed");
        }
    }
    
    @Test
    public void testUserProfile_WithValidToken() {
        clientController.token = "validToken";
        clientController.username = "currentUser";
        
        User profileUser = new User("testUser", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        User currentUser = new User("currentUser", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
  
        List<Post> userPosts = new ArrayList<>();
        Post post1 = new Post(1L, "testUser", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        userPosts.add(post1);
        
        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment(1L, "texto", "commenter", 1L, 43456L);
        comments.add(comment);
        
        List<User> followings = new ArrayList<>();
        User following = new User("followedUser", "USER",false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        followings.add(following);
        
        List<User> followers = new ArrayList<>();
        followers.add(new User("followingUser", "USER",false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false));

        
        // Mock service calls
        when(linkAutoServiceProxy.getUserByUsername(profileUser.username())).thenReturn(profileUser);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(currentUser);
        when(linkAutoServiceProxy.getUserPosts(profileUser.username())).thenReturn(userPosts);
        when(linkAutoServiceProxy.getCommentsByPostId(anyLong())).thenReturn(comments);
        when(linkAutoServiceProxy.getUserByUsername("commenter")).thenReturn(new User("commenter", "USER", false, "Commenter Name", "commenter.jpg", "email", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false));
        when(linkAutoServiceProxy.getUserFollowing("currentUser")).thenReturn(followings);
        when(linkAutoServiceProxy.getUserFollowers(profileUser.username())).thenReturn(followers);
        when(linkAutoServiceProxy.getUserFollowing(profileUser.username())).thenReturn(followings);
        
        String result = clientController.userProfile(profileUser.username(), model);
        
        verify(model).addAttribute("user", profileUser);
        verify(model).addAttribute("currentUser", currentUser);
        verify(model).addAttribute("userPosts", userPosts);
        verify(model).addAttribute(eq("profilePictureByUsername"), any(Map.class));
        verify(model).addAttribute(eq("commentsByPostId"), any(Map.class));
        verify(model).addAttribute(eq("followings"), any(List.class));
        verify(model).addAttribute("followersCount", 1);
        verify(model).addAttribute("followingCount", 1);
        
        assertEquals("userProfile", result);
    }
    
    @Test
    public void testUserProfile_WithInvalidToken() {
        clientController.token = null;
        
        String result = clientController.userProfile("testUser", model);
        
        verifyNoInteractions(model);
        assertEquals("redirect:/", result);
    }
    
    @Test
    public void testSharePost() {
        Post post = new Post(1L, "postOwner", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment(1L, "texto", "commenter", 1L, 43456L);
        comments.add(comment);
        
        when(linkAutoServiceProxy.sharePost(1L)).thenReturn(post);
        when(linkAutoServiceProxy.getCommentsByPostId(1L)).thenReturn(comments);
        when(linkAutoServiceProxy.getUserByUsername("commenter")).thenReturn(new User("commenter", "USER", false , "Commenter Name", "commenter.jpg", "email", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false));
        when(linkAutoServiceProxy.getUserByUsername("postOwner")).thenReturn(new User("postOwner", "USER", false, "Commenter Name", "commenter.jpg", "email", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false));
        
        String result = clientController.sharePost(model, 1L);
        
        verify(model).addAttribute("post", post);
        verify(model).addAttribute(eq("profilePictureByUsername"), any(Map.class));
        verify(model).addAttribute(eq("commentsByPostId"), any(Map.class));
        
        assertEquals("post", result);
    }
    
    @Test
    public void testCommentPost_Success() {
        clientController.token = "validToken";
        
        CommentCreator comment = new CommentCreator("Test comment content");
    
        doNothing().when(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        String result = clientController.commentPost(1L, comment, redirectAttributes);
        
        verify(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        verify(redirectAttributes).addFlashAttribute("success", "Comentario agregado con éxito");
        assertEquals("redirect:/feed", result);
    }
    
    @Test
    public void testCommentPost_Error() {
        clientController.token = "validToken";
        
        CommentCreator comment = new CommentCreator("Test comment content");
        doThrow(new RuntimeException("Error adding comment")).when(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        
        String result = clientController.commentPost(1L, comment, redirectAttributes);
        
        verify(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        verify(redirectAttributes).addFlashAttribute("error", "Error al agregar el comentario: Error adding comment");
        assertEquals("redirect:/feed", result);
    }
    
    @Test
    public void testCommentPostInProfile_Success() {
        clientController.token = "validToken";
        clientController.username = "testUser";
        Post post = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        CommentCreator comment = new CommentCreator("Test comment content");
        
        when(linkAutoServiceProxy.getPostById(1L)).thenReturn(post);
        doNothing().when(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        String result = clientController.commentPostInProfile(1L, comment, redirectAttributes);
        
        verify(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        verify(redirectAttributes).addFlashAttribute("success", "Comentario agregado con éxito");
        assertEquals("redirect:/user/username1", result);
    }
    
    @Test
    public void testCommentPostInProfile_Error() {
        clientController.token = "validToken";
        clientController.username = "testUser";
        Post post = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        CommentCreator comment = new CommentCreator("Test comment content");
        doThrow(new RuntimeException("Error adding comment")).when(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        when(linkAutoServiceProxy.getPostById(1L)).thenReturn(post);
        String result = clientController.commentPostInProfile(1L, comment, redirectAttributes);
        
        verify(linkAutoServiceProxy).commentPost("validToken", 1L, comment);
        verify(redirectAttributes).addFlashAttribute("error", "Error al agregar el comentario: Error adding comment");
        assertEquals("redirect:/user/username1", result);
    }
    
    @Test
    public void testLikePost_Success() {
        clientController.token = "validToken";
        
        String result = clientController.likePost(1L, redirectAttributes);
        
        verify(linkAutoServiceProxy).likePost("validToken", 1L);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación 1 le gusta");
        assertEquals("redirect:/feed", result);
    }
    
    @Test
    public void testLikePost_Error() {
        clientController.token = "validToken";
        
        doThrow(new RuntimeException("Error liking post")).when(linkAutoServiceProxy).likePost("validToken", 1L);
        
        String result = clientController.likePost(1L, redirectAttributes);
        
        verify(linkAutoServiceProxy).likePost("validToken", 1L);
        verify(redirectAttributes).addFlashAttribute("error", "Error al dar me gusta a la publicación: Error liking post");
        assertEquals("redirect:/feed", result);
    }
    
    @Test
    public void testUnlikePost_Success() {
        clientController.token = "validToken";
        
        String result = clientController.unlikePost(1L, redirectAttributes);
        
        verify(linkAutoServiceProxy).unlikePost("validToken", 1L);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación 1 ya no le gusta");
        assertEquals("redirect:/feed", result);
    }
    
    @Test
    public void testUnlikePost_Error() {
        clientController.token = "validToken";
        
        doThrow(new RuntimeException("Error unliking post")).when(linkAutoServiceProxy).unlikePost("validToken", 1L);
        
        String result = clientController.unlikePost(1L, redirectAttributes);
        
        verify(linkAutoServiceProxy).unlikePost("validToken", 1L);
        verify(redirectAttributes).addFlashAttribute("error", "Error al quitar el me gusta a la publicación: Error unliking post");
        assertEquals("redirect:/feed", result);
    }
    @Test
    public void testSearchUsers_WithValidToken_AndValidSearchTerm() {
        clientController.token = "validToken";
        clientController.username = "currentUser";
        String searchTerm = "test";
        
        // Mock the service responses
        List<User> allUsers = new ArrayList<>();
        User user1 = new User("testuser1", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        User user2 = new User("testuser2", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        User user3 = new User("testuser3", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        allUsers.add(user1);
        allUsers.add(user2);
        allUsers.add(user3);
        
        User currentUser = new User("currentUser", "USER", false, "test", "currentUser.jpg", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        
        List<User> followings = new ArrayList<>();
        User following = new User("testUser1", "USER", false, "test", "currentUser.jpg", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        followings.add(following);
        
        when(linkAutoServiceProxy.isUserVerified("testuser3")).thenReturn(true);
        when(linkAutoServiceProxy.getAllUsers()).thenReturn(allUsers);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(currentUser);
        when(linkAutoServiceProxy.getUserFollowing("currentUser")).thenReturn(followings);
        
        // Call the method
        String result = clientController.searchUsers(searchTerm, model, redirectAttributes);
        
        // Verify interactions and result
        verify(model).addAttribute("searchTerm", searchTerm);

        verify(model).addAttribute("currentUser", currentUser);
        verify(model).addAttribute("username", "currentUser");
        verify(model).addAttribute("profilePicture", "currentUser.jpg");
        verify(model).addAttribute("role", "USER");
        verify(model).addAttribute(eq("followings"), any(List.class));
        
        assertEquals("searchResults", result);
    }

    @Test
    public void testSearchUsers_WithValidToken_AndEmptySearchTerm() {
        clientController.token = "validToken";
        String searchTerm = "";
        
        // Call the method
        String result = clientController.searchUsers(searchTerm, model, redirectAttributes);
        
        // No interactions with service should happen
        verifyNoInteractions(linkAutoServiceProxy);
        
        // Should redirect to feed
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testSearchUsers_WithValidToken_AndNullSearchTerm() {
        clientController.token = "validToken";
        
        // Call the method
        String result = clientController.searchUsers(null, model, redirectAttributes);
        
        // No interactions with service should happen
        verifyNoInteractions(linkAutoServiceProxy);
        
        // Should redirect to feed
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testSearchUsers_WithInvalidToken() {
        clientController.token = null;
        String searchTerm = "test";
        
        // Call the method
        String result = clientController.searchUsers(searchTerm, model, redirectAttributes);
        
        // No interactions with model or service should happen
        verifyNoInteractions(model);
        verifyNoInteractions(linkAutoServiceProxy);
        
        // Should redirect to login
        assertEquals("redirect:/", result);
    }

    @Test
    public void testSearchUsers_WithServiceException() {
        clientController.token = "validToken";
        clientController.username = "currentUser";
        String searchTerm = "test";
        
        // Mock service to throw exception
        when(linkAutoServiceProxy.getAllUsers()).thenThrow(new RuntimeException("Service error"));
        
        // Call the method
        String result = clientController.searchUsers(searchTerm, model, redirectAttributes);
        
        // Verify error is added to redirectAttributes
        verify(redirectAttributes).addFlashAttribute("error", "Error al buscar usuarios: Service error");
        
        // Should redirect to feed
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testLikePostInUserProfile() {
        clientController.token = "validToken";
        clientController.username = "testUser";
        
        // Create a custom controller with an overridden method for testing
        ClientController customController = new ClientController() {
            @Override
            public String likePost(Long postId, RedirectAttributes redirectAttributes) {
                try {
                    linkAutoServiceProxy.likePost(token, postId);
                    redirectAttributes.addFlashAttribute("success", "Publicación " + postId + " le gusta");
                    return "redirect:/user/" + username; // Modified to redirect to user profile
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Error al dar me gusta a la publicación: " + e.getMessage());
                    return "redirect:/user/" + username;
                }
            }
        };
        customController.linkAutoServiceProxy = linkAutoServiceProxy;
        customController.token = "validToken";
        customController.username = "testUser";
        
        String result = customController.likePost(1L, redirectAttributes);
        
        verify(linkAutoServiceProxy).likePost("validToken", 1L);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación 1 le gusta");
        assertEquals("redirect:/user/testUser", result);
    }

    @Test
    public void testUnlikePostInUserProfile() {
        clientController.token = "validToken";
        clientController.username = "testUser";
        
        // Create a custom controller with an overridden method for testing
        ClientController customController = new ClientController() {
            @Override
            public String unlikePost(Long postId, RedirectAttributes redirectAttributes) {
                try {
                    linkAutoServiceProxy.unlikePost(token, postId);
                    redirectAttributes.addFlashAttribute("success", "Publicación " + postId + " ya no le gusta");
                    return "redirect:/user/" + username; // Modified to redirect to user profile
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Error al quitar el me gusta a la publicación: " + e.getMessage());
                    return "redirect:/user/" + username;
                }
            }
        };
        customController.linkAutoServiceProxy = linkAutoServiceProxy;
        customController.token = "validToken";
        customController.username = "testUser";
        
        String result = customController.unlikePost(1L, redirectAttributes);
        
        verify(linkAutoServiceProxy).unlikePost("validToken", 1L);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación 1 ya no le gusta");
        assertEquals("redirect:/user/testUser", result);
    }

    @Test
    public void testSharePost_WithValidPostIdAndComments() {
        Post post = new Post(1L, "postOwner", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        // Setup multiple comments
        List<Comment> comments = new ArrayList<>();
        Comment comment1 = new Comment(1L, "texto", "commenter1", 1L, 43456L);
        Comment comment2 = new Comment(2L, "texto", "commenter2", 1L, 43456L);
        comments.add(comment1);
        comments.add(comment2);
        
        // Mock service responses
        when(linkAutoServiceProxy.sharePost(1L)).thenReturn(post);
        when(linkAutoServiceProxy.getCommentsByPostId(1L)).thenReturn(comments);
        when(linkAutoServiceProxy.getUserByUsername("commenter1")).thenReturn(
            new User("commenter1", "USER", false, "Commenter One", "commenter1.jpg", "email1", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false)
        );
        when(linkAutoServiceProxy.getUserByUsername("commenter2")).thenReturn(
            new User("commenter2", "USER", false, "Commenter Two", "commenter2.jpg", "email2", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false)
        );
    
        when(linkAutoServiceProxy.getUserByUsername("postOwner")).thenReturn(new User("postOwner", "USER", false, "Commenter Name", "commenter.jpg", "email", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false));


        // Call the method
        String result = clientController.sharePost(model, 1L);
        
        // Verify interactions and result
        verify(model).addAttribute("post", post);
        verify(model).addAttribute(eq("profilePictureByUsername"), any(Map.class));
        verify(model).addAttribute(eq("commentsByPostId"), any(Map.class));
        
        assertEquals("post", result);
    }

    @Test
    public void testSharePost_WithNoComments() {
        Post post = new Post(1L, "postOwner", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User user = new User("postOwner", "USER", false, "Post owner", "commenter1.jpg", "email1", null, 0, "gender", "location", "password", "desc", new HashSet<>(), false);
        // Empty comments list
        List<Comment> comments = new ArrayList<>();
        
        // Mock service responses
        when(linkAutoServiceProxy.sharePost(1L)).thenReturn(post);
        when(linkAutoServiceProxy.getCommentsByPostId(1L)).thenReturn(comments);
        
        // Call the method
        when(linkAutoServiceProxy.getUserByUsername("postOwner")).thenReturn(user);
        String result = clientController.sharePost(model, 1L);
        
        // Verify interactions and result
        verify(model).addAttribute("post", post);
        verify(model).addAttribute(eq("profilePictureByUsername"), any(Map.class));
        verify(model).addAttribute(eq("commentsByPostId"), any(Map.class));
        
        assertEquals("post", result);
    }

    @Test
    public void testGetAllEvents_WithValidToken() {
        clientController.token = "validToken";
        clientController.username = "testUser";

        // Prepare mock events
        List<Event> events = new ArrayList<>();
        Event event1 = new Event(1L, "user1", "Event 1", "Description 1", "Location 1", 1715000000000L, 1715100000000L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
        Event event2 = new Event(2L, "user2", "Event 2", "Description 2", "Location 2", 1715200000000L, 1715300000000L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
        events.add(event1);
        events.add(event2);

        User user1 = new User("user1", "USER", false, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);
        User user2 = new User("user2", "USER", false, "User Two", "pic2.jpg", "user2@example.com", null, 0, "Female", "Location2", "password", "desc", new HashSet<>(), false);
        User currentUser = new User("testUser", "USER", false, "Test User", "profilePic.jpg", "test@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);

        when(linkAutoServiceProxy.getAllEvents()).thenReturn(events);
        when(linkAutoServiceProxy.getUserByUsername("user1")).thenReturn(user1);
        when(linkAutoServiceProxy.getUserByUsername("user2")).thenReturn(user2);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(currentUser);

        String result = clientController.getAllEvents(model, redirectAttributes);

        verify(model).addAttribute("events", events);
        verify(model).addAttribute("currentUser", currentUser);
        verify(model).addAttribute("username", "testUser");
        verify(model).addAttribute("profilePicture", "profilePic.jpg");
        verify(model).addAttribute("role", "USER");
        verify(model).addAttribute(eq("profilePictureMap"), any(Map.class));
        assertEquals("events", result);
    }

@Test
public void testGetAllEvents_WithInvalidToken() {
    clientController.token = null;
    
    String result = clientController.getAllEvents(model, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para ver los eventos.");
    assertEquals("redirect:/", result);
}

@Test
public void testGetAllEvents_WithException() {
    clientController.token = "validToken";
    
    when(linkAutoServiceProxy.getAllEvents()).thenThrow(new RuntimeException("Error getting events"));
    
    String result = clientController.getAllEvents(model, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Error al obtener los eventos: Error getting events");
    assertEquals("redirect:/feed", result);
}

@Test
public void testGetEventDetails_WithValidToken() {
    clientController.token = "validToken";
    clientController.username = "testUser";
    
    Long eventId = 1L;
    Event event = new Event(eventId, "user1", "Event 1", "Description 1", "Location 1", 1715000000000L, 1715100000000L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    User user = new User("testUser", "USER", false, "Test User", "profilePic.jpg", "test@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);
    
    when(linkAutoServiceProxy.getEventById(eventId)).thenReturn(event);
    when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);
    
    String result = clientController.getEventDetails(eventId, model, redirectAttributes);
    
    verify(model).addAttribute("event", event);
    verify(model).addAttribute("currentUser", user);
    verify(model).addAttribute("username", "testUser");
    verify(model).addAttribute("profilePicture", "profilePic.jpg");
    verify(model).addAttribute("role", "USER");
    
    assertEquals("eventDetails", result);
}

@Test
public void testGetEventDetails_WithInvalidToken() {
    clientController.token = null;
    
    Long eventId = 1L;
    
    String result = clientController.getEventDetails(eventId, model, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para ver los detalles del evento.");
    assertEquals("redirect:/", result);
}

@Test
public void testGetEventDetails_WithException() {
    clientController.token = "validToken";
    
    Long eventId = 1L;
    
    when(linkAutoServiceProxy.getEventById(eventId)).thenThrow(new RuntimeException("Error getting event details"));
    
    String result = clientController.getEventDetails(eventId, model, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Error al obtener los detalles del evento: Error getting event details");
    assertEquals("redirect:/events", result);
}

@Test
public void testShowCreateEventForm_WithValidToken() {
    clientController.token = "validToken";
    clientController.username = "testUser";
    User user = new User("testUser", "USER", false, "Test User", "profilePic.jpg", "test@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);
    
    when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);
    
    String result = clientController.showCreateEventForm(model, redirectAttributes);
    
    verify(model).addAttribute("currentUser", user);
    verify(model).addAttribute("username", "testUser");
    verify(model).addAttribute("profilePicture", "profilePic.jpg");
    verify(model).addAttribute("role", "USER");
    
    assertEquals("createEvent", result);
}

@Test
public void testShowCreateEventForm_WithInvalidToken() {
    clientController.token = null;
    
    String result = clientController.showCreateEventForm(model, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para crear un evento.");
    assertEquals("redirect:/", result);
}

@Test
public void testCreateEvent_Success() {
    clientController.token = "validToken";
    
    EventCreator eventCreator = new EventCreator("Event Title", "Event Description", "Event Location", 1715000000000L, 1715100000000L, new ArrayList<>());
    
    doNothing().when(linkAutoServiceProxy).createEvent("validToken", eventCreator);
    
    String result = clientController.createEvent(eventCreator, redirectAttributes);
    
    verify(linkAutoServiceProxy).createEvent(clientController.token, eventCreator);
    verify(redirectAttributes).addFlashAttribute("success", "Evento creado con éxito");
    assertEquals("redirect:/events", result);
}

@Test
public void testCreateEvent_WithInvalidToken() {
    clientController.token = null;
    
    EventCreator eventCreator = new EventCreator("Event Title", "Event Description", "Event Location", 1715000000000L, 1715100000000L, new ArrayList<>());
    
    String result = clientController.createEvent(eventCreator, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para crear un evento.");
    assertEquals("redirect:/", result);
}

@Test
public void testCreateEvent_Error() {
    clientController.token = "validToken";
    
    EventCreator eventCreator = new EventCreator("Event Title", "Event Description", "Event Location", 1715000000000L, 1715100000000L, new ArrayList<>());
    
    doThrow(new RuntimeException("Error creating event")).when(linkAutoServiceProxy).createEvent("validToken", eventCreator);
    
    String result = clientController.createEvent(eventCreator, redirectAttributes);
    
    verify(linkAutoServiceProxy).createEvent(clientController.token, eventCreator);
    verify(redirectAttributes).addFlashAttribute("error", "Error al crear el evento: Error creating event");
    assertEquals("redirect:/events", result);
}

@Test
public void testDeleteEvent_Success() {
    clientController.token = "validToken";
    Long eventId = 1L;
    
    doNothing().when(linkAutoServiceProxy).deleteEvent("validToken", eventId);
    
    String result = clientController.deleteEvent(eventId, redirectAttributes);
    
    verify(linkAutoServiceProxy).deleteEvent(clientController.token, eventId);
    verify(redirectAttributes).addFlashAttribute("success", "Evento eliminado con éxito.");
    assertEquals("redirect:/events", result);
}

@Test
public void testDeleteEvent_WithInvalidToken() {
    clientController.token = null;
    Long eventId = 1L;
    
    String result = clientController.deleteEvent(eventId, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para eliminar un evento.");
    assertEquals("redirect:/", result);
}

@Test
public void testDeleteEvent_Error() {
    clientController.token = "validToken";
    Long eventId = 1L;
    
    doThrow(new RuntimeException("Error deleting event")).when(linkAutoServiceProxy).deleteEvent("validToken", eventId);
    
    String result = clientController.deleteEvent(eventId, redirectAttributes);
    
    verify(linkAutoServiceProxy).deleteEvent(clientController.token, eventId);
    verify(redirectAttributes).addFlashAttribute("error", "Error al eliminar el evento: Error deleting event");
    assertEquals("redirect:/events", result);
}

@Test
public void testParticipateInEvent_Success() {
    clientController.token = "validToken";
    Long eventId = 1L;
    
    doNothing().when(linkAutoServiceProxy).participateInEvent("validToken", eventId);
    
    String result = clientController.participateInEvent(eventId, redirectAttributes);
    
    verify(linkAutoServiceProxy).participateInEvent(clientController.token, eventId);
    verify(redirectAttributes).addFlashAttribute("success", "Has confirmado tu participación en el evento.");
    assertEquals("redirect:/events", result);
}

@Test
public void testParticipateInEvent_WithInvalidToken() {
    clientController.token = null;
    Long eventId = 1L;
    
    String result = clientController.participateInEvent(eventId, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para participar en un evento.");
    assertEquals("redirect:/", result);
}

@Test
public void testParticipateInEvent_Error() {
    clientController.token = "validToken";
    Long eventId = 1L;
    
    doThrow(new RuntimeException("Error participating in event")).when(linkAutoServiceProxy).participateInEvent("validToken", eventId);
    
    String result = clientController.participateInEvent(eventId, redirectAttributes);
    
    verify(linkAutoServiceProxy).participateInEvent(clientController.token, eventId);
    verify(redirectAttributes).addFlashAttribute("error", "Error al confirmar participación: Error participating in event");
    assertEquals("redirect:/events", result);
}

@Test
public void testCancelParticipation_Success() {
    clientController.token = "validToken";
    Long eventId = 1L;
    
    doNothing().when(linkAutoServiceProxy).cancelParticipation("validToken", eventId);
    
    String result = clientController.cancelParticipation(eventId, redirectAttributes);
    
    verify(linkAutoServiceProxy).cancelParticipation(clientController.token, eventId);
    verify(redirectAttributes).addFlashAttribute("success", "Has cancelado tu participación en el evento.");
    assertEquals("redirect:/events", result);
}

@Test
public void testCancelParticipation_WithInvalidToken() {
    clientController.token = null;
    Long eventId = 1L;
    
    String result = clientController.cancelParticipation(eventId, redirectAttributes);
    
    verify(redirectAttributes).addFlashAttribute("error", "Debes iniciar sesión para cancelar tu participación en un evento.");
    assertEquals("redirect:/", result);
}

@Test
public void testCancelParticipation_Error() {
    clientController.token = "validToken";
    Long eventId = 1L;
    
    doThrow(new RuntimeException("Error canceling participation")).when(linkAutoServiceProxy).cancelParticipation("validToken", eventId);
    
    String result = clientController.cancelParticipation(eventId, redirectAttributes);
    
    verify(linkAutoServiceProxy).cancelParticipation(clientController.token, eventId);
    verify(redirectAttributes).addFlashAttribute("error", "Error al cancelar participación: Error canceling participation");
    assertEquals("redirect:/events", result);
}
    @Test
    public void testVerifyUser_Success() {
        clientController.token = "validToken";
        String usernameToVerify = "testUser";
        String redirectUrl = "/adminPanel";
        
        String result = clientController.verifyUser(usernameToVerify, redirectAttributes, redirectUrl);
        
        verify(linkAutoServiceProxy).verifyUser(clientController.token, usernameToVerify);
        verify(redirectAttributes).addFlashAttribute("success", "Usuario " + usernameToVerify + " verificado con éxito.");
        assertEquals("redirect:/adminPanel", result);
    }
    
    @Test
    public void testVerifyUser_Error() {
        clientController.token = "validToken";
        String usernameToVerify = "testUser";
        String redirectUrl = "/adminPanel";
        
        doThrow(new RuntimeException("Error al verificar al usuario")).when(linkAutoServiceProxy).verifyUser(clientController.token, usernameToVerify);
        
        String result = clientController.verifyUser(usernameToVerify, redirectAttributes, redirectUrl);
        
        verify(linkAutoServiceProxy).verifyUser(clientController.token, usernameToVerify);
        verify(redirectAttributes).addFlashAttribute("error", "Error al verificar al usuario: Error al verificar al usuario");
        assertEquals("redirect:/adminPanel", result);
    }   

    @Test
    public void testSavePost_Success() {
        clientController.token = "validToken";
        Long postId = 1L;

        doNothing().when(linkAutoServiceProxy).savePost("validToken", postId);
        String result = clientController.savePost(postId, redirectAttributes);

        verify(linkAutoServiceProxy).savePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación " + postId + " guardada con éxito");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testSavePost_Error() {
        clientController.token = "validToken";
        Long postId = 1L;

        doThrow(new RuntimeException("Error al guardar la publicación")).when(linkAutoServiceProxy).savePost("validToken", postId);
        String result = clientController.savePost(postId, redirectAttributes);

        verify(linkAutoServiceProxy).savePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("error", "Error al guardar la publicación: Error al guardar la publicación");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testUnsavePost_Success() {
        clientController.token = "validToken";
        Long postId = 1L;

        doNothing().when(linkAutoServiceProxy).unsavePost("validToken", postId);
        String result = clientController.unsavePost(postId, redirectAttributes);

        verify(linkAutoServiceProxy).unsavePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("success", "Publicación " + postId + " eliminada de guardados");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testUnsavePost_Error() {
        clientController.token = "validToken";
        Long postId = 1L;

        doThrow(new RuntimeException("Error al quitar la publicación de guardados")).when(linkAutoServiceProxy).unsavePost("validToken", postId);
        String result = clientController.unsavePost(postId, redirectAttributes);

        verify(linkAutoServiceProxy).unsavePost(clientController.token, postId);
        verify(redirectAttributes).addFlashAttribute("error", "Error al quitar la publicación de guardados: Error al quitar la publicación de guardados");
        assertEquals("redirect:/feed", result);
    }

    @Test
    public void testSavedPosts_WithValidToken() {
        clientController.token = "validToken";
        clientController.username = "testUser";

        List<Post> posts = new ArrayList<>();
        Post post1 = new Post(1L, "user1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(2L, "user2", "content2", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        posts.add(post1);
        posts.add(post2);

        User user = new User("testUser", "USER", false, "Test User", "profilePic.jpg", "test@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false);

        when(linkAutoServiceProxy.getUserSavedPosts("testUser")).thenReturn(posts);
        when(linkAutoServiceProxy.getUserProfile("validToken")).thenReturn(user);
        when(linkAutoServiceProxy.getUserByUsername("user1")).thenReturn(new User("user1", "USER", false, "User One", "pic1.jpg", "user1@example.com", null, 0, "Male", "Location1", "password", "desc", new HashSet<>(), false));
        when(linkAutoServiceProxy.getUserByUsername("user2")).thenReturn(new User("user2", "USER", false, "User Two", "pic2.jpg", "user2@example.com", null, 0, "Female", "Location2", "password", "desc", new HashSet<>(), false));

        List<User> followings = new ArrayList<>();
        followings.add(new User("user3", "USER", false, "User Three", "pic3.jpg", "user3@example.com", null, 0, "Male", "Location3", "password", "desc", new HashSet<>(), false));
        when(linkAutoServiceProxy.getUserFollowing("testUser")).thenReturn(followings);

        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment(1L, "texto", "testuser", 1L, 43456L);
        comments.add(comment);
        when(linkAutoServiceProxy.getCommentsByPostId(anyLong())).thenReturn(comments);

        String result = clientController.savedPosts(model);

        verify(model).addAttribute(eq("profilePictureByUsername"), any(Map.class));
        verify(model).addAttribute("posts", posts);
        verify(model).addAttribute("username", "testUser");
        verify(model).addAttribute("profilePicture", "profilePic.jpg");
        verify(model).addAttribute("role", "USER");
        verify(model).addAttribute(eq("followings"), any(List.class));
        verify(model).addAttribute(eq("commentsByPostId"), any(Map.class));

        assertEquals("savedPost", result);
    }

    @Test
    public void testSavedPosts_WithInvalidToken() {
        clientController.token = null;

        String result = clientController.savedPosts(model);

        verifyNoInteractions(model);
        assertEquals("redirect:/", result);
    }
}
