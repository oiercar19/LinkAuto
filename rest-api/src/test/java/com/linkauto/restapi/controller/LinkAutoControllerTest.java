package com.linkauto.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.CommentReturnerDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.dto.UserDTO;
import com.linkauto.restapi.dto.UserReturnerDTO;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.Role;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.service.AuthService;
import com.linkauto.restapi.service.LinkAutoService;

public class LinkAutoControllerTest {

    LinkAutoService linkAutoService;
    AuthService authService;
    LinkAutoController linkAutoController;
    User usuario;

    @BeforeEach
    public void setUp() {
        linkAutoService = mock(LinkAutoService.class);
        authService = mock(AuthService.class);
        linkAutoController = new LinkAutoController(linkAutoService, authService);
        usuario = new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
    }
    
    @Test
    public void testGetAllPosts() {
        List<Post> posts = new ArrayList<>();

        Post post = new Post(1L, usuario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Comment comment = new Comment("testComment", usuario, post, 9999L);
        Comment comment2 = new Comment("testComment2", usuario, post, 9999L);
        post.addComentario(comment);
        post.addComentario(comment2);
        
        Post post2 = new Post(2L, usuario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        posts.add(post);
        posts.add(post2);
        when(linkAutoService.getAllPosts()).thenReturn(posts);
        
        ResponseEntity<List<PostReturnerDTO>> result = linkAutoController.getAllPosts();
        
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
    }

    @Test
    public void testGetPostById() {
        Post post = new Post(1L, usuario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(linkAutoService.getPostById(1L)).thenReturn(Optional.of(post));
        ResponseEntity<PostReturnerDTO> result = linkAutoController.getPostById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());

        when(linkAutoService.getPostById(2L)).thenReturn(Optional.empty());
        ResponseEntity<PostReturnerDTO> result2 = linkAutoController.getPostById(2L);
        assertEquals(HttpStatus.NOT_FOUND, result2.getStatusCode());
    }

    @Test
    public void createPost(){
        String userToken = "1234";

        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<PostReturnerDTO> result = linkAutoController.createPost(userToken, new PostDTO("testPost", Arrays.asList("img1", "img2")));
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        Post post = new Post(1L, usuario, "testPost", 1234567, Arrays.asList("img1", "img2"), new ArrayList<>(), new HashSet<>());
        post.addComentario(new Comment("testComment", usuario, post, 9999L));
        post.addComentario(new Comment("testComment2", usuario, post, 9999L));
        PostDTO postDTO = new PostDTO("testPost", Arrays.asList("img1", "img2"));
        when(linkAutoService.createPost(postDTO, usuario)).thenReturn(post);
        ResponseEntity<PostReturnerDTO> result2 = linkAutoController.createPost(userToken, postDTO);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertNotNull(result2.getBody());
        assertEquals("testPost", result2.getBody().getMessage());
        assertEquals(1L, result2.getBody().getId());
        assertEquals(1234567, result2.getBody().getCreationDate());
        assertEquals(Arrays.asList("img1", "img2"), result2.getBody().getImages());
    }

    @Test
    public void testDeletePost() {
        String userToken = "1234";
        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<Void> result = linkAutoController.deletePost(1L, userToken);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.deletePost(2L, usuario)).thenReturn(true);
        ResponseEntity<Void> result2 = linkAutoController.deletePost(2L, userToken);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertDoesNotThrow(() -> linkAutoController.deletePost(2L, userToken));
        verify(linkAutoService, times(2)).deletePost(2L, usuario);

        when(linkAutoService.deletePost(3L, usuario)).thenReturn(false);
        ResponseEntity<Void> result3 = linkAutoController.deletePost(3L, userToken);
        assertEquals(HttpStatus.NOT_FOUND, result3.getStatusCode());
        verify(linkAutoService, times(1)).deletePost(3L, usuario);
    }

    @Test
    public void testGetUserFollowers(){
        List<User> followers = new ArrayList<>();
        followers.add(usuario);
        followers.add(new User("usuario2", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),new HashSet<>())); 
        when(linkAutoService.getFollowersByUsername("test")).thenReturn(followers);
        ResponseEntity<List<UserReturnerDTO>> result = linkAutoController.getUserFollowers("test");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals("usuario2", result.getBody().get(1).getUsername());
    }

    @Test
    public void testGetUserFollowing(){
        List<User> following = new ArrayList<>();
        following.add(usuario);
        following.add(new User("usuario2", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>())); 
        when(linkAutoService.getFollowingByUsername("test")).thenReturn(following);
        ResponseEntity<List<UserReturnerDTO>> result = linkAutoController.getUserFollowing("test");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals("usuario2", result.getBody().get(1).getUsername());
    }

    @Test
    public void testFollowUser() {
        String userToken = "1234";
        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<Void> result = linkAutoController.followUser("test", userToken);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.followUser(usuario, "test")).thenReturn(true);
        ResponseEntity<Void> result2 = linkAutoController.followUser("test", userToken);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertDoesNotThrow(() -> linkAutoController.followUser("test", userToken));
        verify(linkAutoService, times(2)).followUser(usuario, "test");
        
        when(linkAutoService.followUser(usuario, "test3")).thenReturn(false);
        ResponseEntity<Void> result3 = linkAutoController.followUser("test3", userToken);
        assertEquals(HttpStatus.NOT_FOUND, result3.getStatusCode());
        verify(linkAutoService, times(1)).followUser(usuario, "test3");
    }

    @Test
    public void testUnfollowUser() {
        String userToken = "1234";
        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<Void> result = linkAutoController.unfollowUser("test", userToken);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        User test = new User("test", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        test.addFollower(usuario);
        usuario.addFollowing(test);
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.unfollowUser(usuario, "test")).thenReturn(true);
        ResponseEntity<Void> result2 = linkAutoController.unfollowUser("test", userToken);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertDoesNotThrow(() -> linkAutoController.unfollowUser("test", userToken));
        verify(linkAutoService, times(2)).unfollowUser(usuario, "test");
        
        when(linkAutoService.unfollowUser(usuario, "test3")).thenReturn(false);
        ResponseEntity<Void> result3 = linkAutoController.unfollowUser("test3", userToken);
        assertEquals(HttpStatus.NOT_FOUND, result3.getStatusCode());
        verify(linkAutoService, times(1)).unfollowUser(usuario, "test3");
    }

    @Test
    public void testDeleteUser() {
        String userToken = "1234";
        String targetUsername = "targetUser";

        // Scenario 1: Invalid token
        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<Void> result = linkAutoController.deleteUser(userToken, targetUsername);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        verify(authService, times(1)).isTokenValid(userToken);

        // Scenario 2: Logged-in user not authorized to delete target user
        when(authService.isTokenValid(userToken)).thenReturn(true);
        User loggedUser = new User("loggedUser", "name", "profilePic", "email", new ArrayList<>(), 123456L, Gender.MALE, "location", "password", "description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(authService.getUserByToken(userToken)).thenReturn(loggedUser);
        ResponseEntity<Void> result2 = linkAutoController.deleteUser(userToken, targetUsername);
        assertEquals(HttpStatus.FORBIDDEN, result2.getStatusCode());
        verify(authService, times(1)).getUserByToken(userToken);

        // Scenario 3: Target user does not exist
        loggedUser.setRole(Role.ADMIN); // Make logged user an admin
        when(authService.getUserByUsername(targetUsername)).thenReturn(null);
        ResponseEntity<Void> result3 = linkAutoController.deleteUser(userToken, targetUsername);
        assertEquals(HttpStatus.NOT_FOUND, result3.getStatusCode());
        verify(authService, times(1)).getUserByUsername(targetUsername);

        // Scenario 4: Successful deletion
        User targetUser = new User(targetUsername, "name", "profilePic", "email", new ArrayList<>(), 123456L, Gender.MALE, "location", "password", "description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(authService.getUserByUsername(targetUsername)).thenReturn(targetUser);
        when(authService.deleteUser(targetUser, userToken)).thenReturn(true);
        ResponseEntity<Void> result4 = linkAutoController.deleteUser(userToken, targetUsername);
        assertEquals(HttpStatus.OK, result4.getStatusCode());
        verify(authService, times(1)).deleteUser(targetUser, userToken);

        // Scenario 5: Deletion failed
        when(authService.deleteUser(targetUser, userToken)).thenReturn(false);
        ResponseEntity<Void> result5 = linkAutoController.deleteUser(userToken, targetUsername);
        assertEquals(HttpStatus.NOT_FOUND, result5.getStatusCode());
        verify(authService, times(2)).deleteUser(targetUser, userToken);

        // Scenario 6: User trying to delete themselves
        when(authService.getUserByToken(userToken)).thenReturn(targetUser);
        when(authService.getUserByUsername(targetUser.getUsername())).thenReturn(targetUser);
        when(authService.deleteUser(targetUser, userToken)).thenReturn(true);
        ResponseEntity<Void> result6 = linkAutoController.deleteUser(userToken, targetUsername);
        assertEquals(HttpStatus.OK, result6.getStatusCode());
        verify(authService, times(3)).deleteUser(targetUser, userToken);
    }

    @Test
    void testPromoteToAdmin() {
        // Arrange
        String userToken = "validToken";
        String invalidToken = "invalidToken";
        String username = "johndoe";

        User requestingUser = new User();
        requestingUser.setRole(Role.ADMIN);
        requestingUser.setUsername("requestingUser");

        User targetUser = new User();
        targetUser.setUsername("targetUser");

        // Case 1: Invalid token
        when(authService.isTokenValid(invalidToken)).thenReturn(false);

        ResponseEntity<Void> responseInvalidToken = linkAutoController.promoteToAdmin(username, invalidToken);
        assertEquals(HttpStatus.UNAUTHORIZED, responseInvalidToken.getStatusCode());
        verify(authService, times(1)).isTokenValid(invalidToken);

        // Case 2: Requesting user is not an admin
        when(authService.isTokenValid(userToken)).thenReturn(true);
        requestingUser.setRole(Role.USER);
        when(authService.getUserByToken(userToken)).thenReturn(requestingUser);

        ResponseEntity<Void> responseNotAdmin = linkAutoController.promoteToAdmin(username, userToken);
        assertEquals(HttpStatus.FORBIDDEN, responseNotAdmin.getStatusCode());
        verify(authService, times(1)).getUserByToken(userToken);

        // Case 3: Target user not found
        requestingUser.setRole(Role.ADMIN);
        when(authService.getUserByToken(userToken)).thenReturn(requestingUser);
        when(authService.getUserByUsername(username)).thenReturn(null);

        ResponseEntity<Void> responseUserNotFound = linkAutoController.promoteToAdmin(username, userToken);
        assertEquals(HttpStatus.NOT_FOUND, responseUserNotFound.getStatusCode());
        verify(authService, times(1)).getUserByUsername(username);

        // Case 4: Successful promotion
        when(authService.getUserByUsername(username)).thenReturn(targetUser);
        when(authService.changeRole(targetUser, Role.ADMIN)).thenReturn(true);

        ResponseEntity<Void> responseSuccess = linkAutoController.promoteToAdmin(username, userToken);
        assertEquals(HttpStatus.OK, responseSuccess.getStatusCode());
        verify(authService, times(1)).changeRole(targetUser, Role.ADMIN);

        // Case 5: User promoting themselves
        when(authService.getUserByToken(userToken)).thenReturn(requestingUser);
        when(authService.getUserByUsername(requestingUser.getUsername())).thenReturn(requestingUser);
        ResponseEntity<Void> responseSelfPromotion = linkAutoController.promoteToAdmin(requestingUser.getUsername(), userToken);
        assertEquals(HttpStatus.FORBIDDEN, responseSelfPromotion.getStatusCode());
        verify(authService, times(1)).getUserByUsername(requestingUser.getUsername());
        
        // Case 6: Promotion failed
        when(authService.changeRole(targetUser, Role.ADMIN)).thenReturn(false);
        ResponseEntity<Void> responsePromotionFailed = linkAutoController.promoteToAdmin(username, userToken);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responsePromotionFailed.getStatusCode());
    }

    @Test
    void testDemoteToUser() {
        // Arrange
        String userToken = "validToken";
        String invalidToken = "invalidToken";
        String username = "johndoe";

        User requestingUser = new User();
        requestingUser.setRole(Role.ADMIN);
        requestingUser.setUsername("requestingUser");

        User targetUser = new User();
        targetUser.setUsername("targetUser");

        // Case 1: Invalid token
        when(authService.isTokenValid(invalidToken)).thenReturn(false);

        ResponseEntity<Void> responseInvalidToken = linkAutoController.demoteToUser(username, invalidToken);
        assertEquals(HttpStatus.UNAUTHORIZED, responseInvalidToken.getStatusCode());
        verify(authService, times(1)).isTokenValid(invalidToken);

        // Case 2: Requesting user is not an admin
        when(authService.isTokenValid(userToken)).thenReturn(true);
        requestingUser.setRole(Role.USER);
        when(authService.getUserByToken(userToken)).thenReturn(requestingUser);

        ResponseEntity<Void> responseNotAdmin = linkAutoController.demoteToUser(username, userToken);
        assertEquals(HttpStatus.FORBIDDEN, responseNotAdmin.getStatusCode());
        verify(authService, times(1)).getUserByToken(userToken);

        // Case 3: Target user not found
        requestingUser.setRole(Role.ADMIN);
        when(authService.getUserByToken(userToken)).thenReturn(requestingUser);
        when(authService.getUserByUsername(username)).thenReturn(null);

        ResponseEntity<Void> responseUserNotFound = linkAutoController.demoteToUser(username, userToken);
        assertEquals(HttpStatus.NOT_FOUND, responseUserNotFound.getStatusCode());
        verify(authService, times(1)).getUserByUsername(username);

        // Case 4: Successful demotion
        when(authService.getUserByUsername(username)).thenReturn(targetUser);
        when(authService.changeRole(targetUser, Role.USER)).thenReturn(true);

        ResponseEntity<Void> responseSuccess = linkAutoController.demoteToUser(username, userToken);
        assertEquals(HttpStatus.OK, responseSuccess.getStatusCode());
        verify(authService, times(1)).changeRole(targetUser, Role.USER);

        // Case 5: User demoting themselves
        when(authService.getUserByToken(userToken)).thenReturn(requestingUser);
        when(authService.getUserByUsername(requestingUser.getUsername())).thenReturn(requestingUser);
        ResponseEntity<Void> responseSelfDemotion = linkAutoController.demoteToUser(requestingUser.getUsername(), userToken);
        assertEquals(HttpStatus.FORBIDDEN, responseSelfDemotion.getStatusCode());
        verify(authService, times(1)).getUserByUsername(requestingUser.getUsername());

        // Case 6: Demotion failed
        when(authService.changeRole(targetUser, Role.USER)).thenReturn(false);
        ResponseEntity<Void> responseDemotionFailed = linkAutoController.demoteToUser(username, userToken);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseDemotionFailed.getStatusCode());

    }

    @Test
    public void testGetUserDetails() {
        String userToken = "validToken";
        
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        
        ResponseEntity<UserReturnerDTO> response = linkAutoController.getUserDetails(userToken);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ownerUsername", response.getBody().getUsername());
        assertEquals("ownerName", response.getBody().getName());
        assertEquals("ownerEmail", response.getBody().getEmail());
        // Test null user scenario
        when(authService.getUserByToken("invalidToken")).thenReturn(null);
        ResponseEntity<UserReturnerDTO> nullResponse = linkAutoController.getUserDetails("invalidToken");
        assertEquals(HttpStatus.UNAUTHORIZED, nullResponse.getStatusCode());
    }

    @Test
    public void testUpdateUser() {
        String userToken = "validToken";
        UserDTO userDto = new UserDTO("updatedName", "USER", "avatar.jpg" ,"updatedEmail", 
                                    new ArrayList<>(), 123456L, "male", "updatedLocation",
                                    "updatedPassword", "updatedDescription");
        
        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<User> invalidResponse = linkAutoController.updateUser("invalidToken", userDto);
        assertEquals(HttpStatus.UNAUTHORIZED, invalidResponse.getStatusCode());
        
        // Case 2: Valid update by same user
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario).thenReturn(usuario);
        when(authService.updateUser(any(User.class), eq(userToken))).thenReturn(true);
        
        ResponseEntity<User> validResponse = linkAutoController.updateUser(userToken, userDto);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        assertNotNull(validResponse.getBody());
        assertEquals("updatedName", validResponse.getBody().getName());
        
        // Case 3: Regular user trying to update role
        UserDTO roleSwitchDto = new UserDTO("name", "ADMIN", "pic", "email", 
                                        new ArrayList<>(), 123456L, "male", "location",
                                        "password", "description");
        
        ResponseEntity<User> forbiddenResponse = linkAutoController.updateUser(userToken, roleSwitchDto);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenResponse.getStatusCode());
        
        // Case 4: Admin updating another user
        User adminUser = new User("adminUsername", "adminName", "adminPic", "adminEmail", 
                                new ArrayList<>(), 123456L, Gender.MALE, "adminLocation", 
                                "adminPassword", "adminDescription", new ArrayList<>(), 
                                new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        adminUser.setRole(Role.ADMIN);
        
        when(authService.getUserByToken(userToken)).thenReturn(adminUser).thenReturn(usuario);
        ResponseEntity<User> adminUpdateResponse = linkAutoController.updateUser(userToken, userDto);
        assertEquals(HttpStatus.OK, adminUpdateResponse.getStatusCode());
        
        // Case 5: Update failed
        when(authService.updateUser(any(User.class), eq(userToken))).thenReturn(false);
        ResponseEntity<User> failedResponse = linkAutoController.updateUser(userToken, userDto);
        assertEquals(HttpStatus.NOT_FOUND, failedResponse.getStatusCode());

        // Case 6: Random user trying to update another user
        when(authService.isTokenValid("randomUser1Token")).thenReturn(true);
        User randomUser1 = new User("randomUser1", "randomUser1", "adminPic", "adminEmail", new ArrayList<>(), 123456L, Gender.MALE, "adminLocation", "adminPassword", "adminDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(authService.getUserByToken("randomUser1Token")).thenReturn(randomUser1).thenReturn(usuario);
        ResponseEntity<User> randomUserResponse = linkAutoController.updateUser("randomUser1Token", userDto);
        assertEquals(HttpStatus.FORBIDDEN, randomUserResponse.getStatusCode());
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(usuario);
        users.add(new User("user2", "name2", "pic2", "email2", 
                        new ArrayList<>(), 123456L, Gender.FEMALE, "location2", 
                        "password2", "description2", new ArrayList<>(), 
                        new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
        
        when(linkAutoService.getAllUsers()).thenReturn(users);
        
        ResponseEntity<List<UserReturnerDTO>> response = linkAutoController.getAllUsers();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("ownerUsername", response.getBody().get(0).getUsername());
        assertEquals("user2", response.getBody().get(1).getUsername());
    }

    @Test
    public void testGetUserPosts() {
        String username = "testUser";
        List<Post> posts = new ArrayList<>();
        
        Post post1 = new Post(1L, usuario, "message1", 1234567, 
                            new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(2L, usuario, "message2", 1234568, 
                            new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        posts.add(post1);
        posts.add(post2);
        
        when(linkAutoService.getPostsByUsername(username)).thenReturn(posts);
        
        ResponseEntity<List<PostReturnerDTO>> response = linkAutoController.getUserPosts(username);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(2L, response.getBody().get(1).getId());
    }

    @Test
    public void testGetUserByUsername() {
        String username = "testUser";
        User testUser = new User(username, "testName", "testPic", "testEmail", 
                                new ArrayList<>(), 123456L, Gender.MALE, "testLocation", 
                                "testPassword", "testDescription", new ArrayList<>(), 
                                new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(linkAutoService.getUserByUsername(username)).thenReturn(Optional.of(testUser));
        
        UserReturnerDTO response = linkAutoController.getUserByUsername(username);
        
        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals("testName", response.getName());
        assertEquals("testEmail", response.getEmail());

        // Case 2: User is null
        when(linkAutoService.getUserByUsername("nonExistentUser")).thenReturn(Optional.empty());
        UserReturnerDTO nullResponse = linkAutoController.getUserByUsername("nonExistentUser");
        assertNull(nullResponse);

    }

    @Test
    public void testLikePost() {
        String userToken = "validToken";
        Long postId = 1L;
        
        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<Void> invalidResponse = linkAutoController.likePost(postId, "invalidToken");
        assertEquals(HttpStatus.UNAUTHORIZED, invalidResponse.getStatusCode());
        
        // Case 2: Valid like
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.likePost(postId, usuario.getUsername())).thenReturn(true);
        
        ResponseEntity<Void> validResponse = linkAutoController.likePost(postId, userToken);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        
        // Case 3: Post not found
        when(linkAutoService.likePost(2L, usuario.getUsername())).thenReturn(false);
        ResponseEntity<Void> notFoundResponse = linkAutoController.likePost(2L, userToken);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    public void testUnlikePost() {
        String userToken = "validToken";
        Long postId = 1L;
        
        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<Void> invalidResponse = linkAutoController.unlikePost(postId, "invalidToken");
        assertEquals(HttpStatus.UNAUTHORIZED, invalidResponse.getStatusCode());
        
        // Case 2: Valid unlike
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.unlikePost(postId, usuario.getUsername())).thenReturn(true);
        
        ResponseEntity<Void> validResponse = linkAutoController.unlikePost(postId, userToken);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        
        // Case 3: Post not found or not liked
        when(linkAutoService.unlikePost(2L, usuario.getUsername())).thenReturn(false);
        ResponseEntity<Void> notFoundResponse = linkAutoController.unlikePost(2L, userToken);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    public void testCommentPost() {
        String userToken = "validToken";
        Long postId = 1L;
        CommentDTO commentDto = new CommentDTO("Test comment");
        
        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<Void> invalidResponse = linkAutoController.commentPost(postId, "invalidToken", commentDto);
        assertEquals(HttpStatus.UNAUTHORIZED, invalidResponse.getStatusCode());
        
        // Case 2: Valid comment
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.commentPost(postId, usuario, commentDto)).thenReturn(true);
        
        ResponseEntity<Void> validResponse = linkAutoController.commentPost(postId, userToken, commentDto);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        
        // Case 3: Post not found
        when(linkAutoService.commentPost(2L, usuario, commentDto)).thenReturn(false);
        ResponseEntity<Void> notFoundResponse = linkAutoController.commentPost(2L, userToken, commentDto);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    public void testGetAllComments() {
        List<Comment> comments = new ArrayList<>();
        Post post = new Post(1L, usuario, "testMessage", 1234567, 
                            new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        Comment comment1 = new Comment("comment1", usuario, post, 9999L);
        comment1.setId(1L);
        Comment comment2 = new Comment("comment2", usuario, post, 9998L);
        comment2.setId(2L);
        
        comments.add(comment1);
        comments.add(comment2);
        
        when(linkAutoService.getAllComments()).thenReturn(comments);
        
        ResponseEntity<List<CommentReturnerDTO>> response = linkAutoController.getAllComments();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("comment1", response.getBody().get(0).getText());
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals("comment2", response.getBody().get(1).getText());
    }

    @Test
    public void testGetCommentsByPostId() {
        Long postId = 1L;
        List<Comment> comments = new ArrayList<>();
        Post post = new Post(postId, usuario, "testMessage", 1234567, 
                            new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        Comment comment1 = new Comment("comment1", usuario, post, 9999L);
        comment1.setId(1L);
        Comment comment2 = new Comment("comment2", usuario, post, 9998L);
        comment2.setId(2L);
        
        comments.add(comment1);
        comments.add(comment2);
        
        when(linkAutoService.getCommentsByPostId(postId)).thenReturn(comments);
        
        ResponseEntity<List<CommentReturnerDTO>> response = linkAutoController.getCommentsByPostId(postId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(2L, response.getBody().get(1).getId());
    }
    

    @Test
    public void testSavePost_Unauthorized() {
        Long postId = 1L;
        String userToken = "invalidToken";

        when(authService.isTokenValid(userToken)).thenReturn(false);

        ResponseEntity<Void> response = linkAutoController.savePost(postId, userToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService, times(1)).isTokenValid(userToken);
    }

    @Test
    public void testSavePost_Success() {
        Long postId = 1L;
        String userToken = "validToken";

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.savePost(postId, usuario)).thenReturn(true);

        ResponseEntity<Void> response = linkAutoController.savePost(postId, userToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(linkAutoService, times(1)).savePost(eq(postId), any(User.class));
    }

    @Test
    public void testSavePost_NotFound() {
        Long postId = 99L;
        String userToken = "validToken";

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.savePost(postId, usuario)).thenReturn(false);

        ResponseEntity<Void> response = linkAutoController.savePost(postId, userToken);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(linkAutoService, times(1)).savePost(postId, usuario);
    }

    @Test
    public void testUnsavePost() {
        Long postId = 1L;
        String userToken = "1234567890";

        // Caso 1: Token inválido
        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<Void> responseInvalid = linkAutoController.unsavePost(postId, userToken);
        assertEquals(HttpStatus.UNAUTHORIZED, responseInvalid.getStatusCode());
        verify(authService, times(1)).isTokenValid(userToken);

        // Caso 2: Unsave exitoso
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        when(linkAutoService.unsavePost(postId, usuario)).thenReturn(true);

        ResponseEntity<Void> responseSuccess = linkAutoController.unsavePost(postId, userToken);
        assertEquals(HttpStatus.OK, responseSuccess.getStatusCode());
        verify(linkAutoService, times(1)).unsavePost(postId, usuario);

        // Caso 3: Post no encontrado o no guardado
        when(linkAutoService.unsavePost(postId, usuario)).thenReturn(false);
        ResponseEntity<Void> responseNotFound = linkAutoController.unsavePost(postId, userToken);
        assertEquals(HttpStatus.NOT_FOUND, responseNotFound.getStatusCode());
        verify(linkAutoService, times(2)).unsavePost(postId, usuario);
    }

    @Test
    void testGetSavedPostsByUsername() {
        // Arrange
        String username = "johndoe";
    
        User user = new User();
        user.setUsername(username); // o usa constructor si tienes
    
        Post post1 = new Post();
        post1.setUsuario(user);
    
        Post post2 = new Post();
        post2.setUsuario(user);
    
        List<Post> savedPosts = List.of(post1, post2);
    
        when(linkAutoService.getSavedPostsByUsername(username)).thenReturn(savedPosts);
    
        // Act
        ResponseEntity<List<PostReturnerDTO>> response = linkAutoController.getSavedPostsByUsername(username);
    
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }



}
