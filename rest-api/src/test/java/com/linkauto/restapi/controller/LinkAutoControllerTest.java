package com.linkauto.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.CommentReturnerDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.dto.UpdateUserDTO;
import com.linkauto.restapi.dto.EventDTO;
import com.linkauto.restapi.dto.EventReturnerDTO;
import com.linkauto.restapi.dto.UserReturnerDTO;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.Role; 
import com.linkauto.restapi.model.Event; 
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
        when(linkAutoService.followUser(usuario.getUsername(), "test")).thenReturn(true);
        ResponseEntity<Void> result2 = linkAutoController.followUser("test", userToken);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertDoesNotThrow(() -> linkAutoController.followUser("test", userToken));
        verify(linkAutoService, times(2)).followUser(usuario.getUsername(), "test");
        
        when(linkAutoService.followUser(usuario.getUsername(), "test3")).thenReturn(false);
        ResponseEntity<Void> result3 = linkAutoController.followUser("test3", userToken);
        assertEquals(HttpStatus.NOT_FOUND, result3.getStatusCode());
        verify(linkAutoService, times(1)).followUser(usuario.getUsername(), "test3");
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
        UpdateUserDTO userDto = new UpdateUserDTO("updatedUsername", "updatedName", "avatar.jpg" ,"updatedEmail", 
                                    new ArrayList<>(), 123456L, "male", "updatedLocation",
                                    "updatedPassword", "updatedDescription");
        
        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<UpdateUserDTO> invalidResponse = linkAutoController.updateUser("invalidToken", userDto);
        assertEquals(HttpStatus.UNAUTHORIZED, invalidResponse.getStatusCode());
        
        // Case 2: Valid update by same user
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario).thenReturn(usuario);
        when(authService.updateUser(any(User.class), eq(userToken))).thenReturn(true);
        
        ResponseEntity<UpdateUserDTO> validResponse = linkAutoController.updateUser(userToken, userDto);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        assertNotNull(validResponse.getBody());
        assertEquals("updatedName", validResponse.getBody().getName());

        // Case 3: Update failed
        when(authService.updateUser(any(User.class), eq(userToken))).thenReturn(false);
        ResponseEntity<UpdateUserDTO> failedResponse = linkAutoController.updateUser(userToken, userDto);
        assertEquals(HttpStatus.NOT_FOUND, failedResponse.getStatusCode());
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
    public void testBanUser_Success() {
        // Datos de prueba
        String username = "testUser";
        boolean banStatus = true;
        String userToken = "validToken";
                // Crear usuarios de prueba
        User adminUser = new User("admin", "Admin Name", "adminPic", "adminEmail",
                new ArrayList<>(), 123456L, User.Gender.MALE, "adminLocation", "adminPassword",
                "adminDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        adminUser.setRole(Role.ADMIN);
    
        User targetUser = new User(username, "Target Name", "targetPic", "targetEmail",
                new ArrayList<>(), 123456L, User.Gender.FEMALE, "targetLocation", "targetPassword",
                "targetDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
    
        // Mock del servicio
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(adminUser);
        when(authService.getUserByUsername(username)).thenReturn(targetUser);
        when(authService.banUser(username, banStatus)).thenReturn(true);
    
        // Llamar al método del controlador
        ResponseEntity<Void> response = linkAutoController.banUser(username, banStatus, userToken);
    
        // Verificar el resultado
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService).getUserByToken(userToken);
        verify(authService).getUserByUsername(username);
        verify(authService).banUser(username, banStatus);
    }
    
   @Test
public void testGetAllEvents() {
    List<Event> events = new ArrayList<>();
    User creator = usuario;
    
    Event event1 = new Event(1L, creator, "Event 1", "Description 1", "Location 1", 
                           1234567L, 1234568L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    Event event2 = new Event(2L, creator, "Event 2", "Description 2", "Location 2", 
                           1234569L, 1234570L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    
    events.add(event1);
    events.add(event2);
    
    when(linkAutoService.getAllEvents()).thenReturn(events);
    
    ResponseEntity<List<EventReturnerDTO>> result = linkAutoController.getAllEvents();
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(2, result.getBody().size());
    assertEquals("Event 1", result.getBody().get(0).getTitle());
    assertEquals("Event 2", result.getBody().get(1).getTitle());
}

@Test
public void testGetEventById() {
    Long eventId = 1L;
    User creator = usuario;
    
    Event event = new Event(eventId, creator, "Test Event", "Test Description", "Test Location", 
                          1234567L, 1234568L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    
    when(linkAutoService.getEventById(eventId)).thenReturn(Optional.of(event));
    
    ResponseEntity<EventReturnerDTO> result = linkAutoController.getEventById(eventId);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(eventId, result.getBody().getId());
    assertEquals("Test Event", result.getBody().getTitle());
    
    // Test not found scenario
    when(linkAutoService.getEventById(2L)).thenReturn(Optional.empty());
    ResponseEntity<EventReturnerDTO> notFoundResult = linkAutoController.getEventById(2L);
    assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
}

@Test
public void testCreateEvent() {
    String userToken = "validToken";
    List<String> images = Arrays.asList("img1.jpg", "img2.jpg");
    EventDTO eventDTO = new EventDTO("New Event", "New Description", "New Location", 
                                  1234567L, 1234568L, images);
    
    // Test unauthorized scenario
    when(authService.isTokenValid("invalidToken")).thenReturn(false);
    ResponseEntity<EventReturnerDTO> unauthorizedResult = linkAutoController.createEvent("invalidToken", eventDTO);
    assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResult.getStatusCode());
    
    // Test successful creation
    when(authService.isTokenValid(userToken)).thenReturn(true);
    when(authService.getUserByToken(userToken)).thenReturn(usuario);
    
    Event createdEvent = new Event(1L, usuario, "New Event", "New Description", "New Location", 
                                 1234567L, 1234568L, images, new HashSet<>(), new ArrayList<>());
    
    when(linkAutoService.createEvent(eventDTO, usuario)).thenReturn(createdEvent);
    
    ResponseEntity<EventReturnerDTO> result = linkAutoController.createEvent(userToken, eventDTO);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals("New Event", result.getBody().getTitle());
    assertEquals("New Description", result.getBody().getDescription());
    assertEquals(1L, result.getBody().getId());
}

@Test
public void testDeleteEvent() {
    String userToken = "validToken";
    Long eventId = 1L;
    
    // Test unauthorized scenario
    when(authService.isTokenValid("invalidToken")).thenReturn(false);
    ResponseEntity<Void> unauthorizedResult = linkAutoController.deleteEvent(eventId, "invalidToken");
    assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResult.getStatusCode());
    
    // Test successful deletion
    when(authService.isTokenValid(userToken)).thenReturn(true);
    when(authService.getUserByToken(userToken)).thenReturn(usuario);
    when(linkAutoService.deleteEvent(eventId, usuario)).thenReturn(true);
    
    ResponseEntity<Void> result = linkAutoController.deleteEvent(eventId, userToken);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(linkAutoService, times(1)).deleteEvent(eventId, usuario);
    
    // Test not found scenario
    when(linkAutoService.deleteEvent(2L, usuario)).thenReturn(false);
    ResponseEntity<Void> notFoundResult = linkAutoController.deleteEvent(2L, userToken);
    assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
}

@Test
public void testParticipateInEvent() {
    String userToken = "validToken";
    Long eventId = 1L;
    
    // Test unauthorized scenario
    when(authService.isTokenValid("invalidToken")).thenReturn(false);
    ResponseEntity<Void> unauthorizedResult = linkAutoController.participateInEvent(eventId, "invalidToken");
    assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResult.getStatusCode());
    
    // Test successful participation
    when(authService.isTokenValid(userToken)).thenReturn(true);
    when(authService.getUserByToken(userToken)).thenReturn(usuario);
    when(linkAutoService.participateInEvent(eventId, usuario)).thenReturn(true);
    
    ResponseEntity<Void> result = linkAutoController.participateInEvent(eventId, userToken);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(linkAutoService, times(1)).participateInEvent(eventId, usuario);
    
    // Test not found scenario
    when(linkAutoService.participateInEvent(2L, usuario)).thenReturn(false);
    ResponseEntity<Void> notFoundResult = linkAutoController.participateInEvent(2L, userToken);
    assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
}

@Test
public void testCancelParticipation() {
    String userToken = "validToken";
    Long eventId = 1L;
    
    // Test unauthorized scenario
    when(authService.isTokenValid("invalidToken")).thenReturn(false);
    ResponseEntity<Void> unauthorizedResult = linkAutoController.cancelParticipation(eventId, "invalidToken");
    assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResult.getStatusCode());
    
    // Test successful cancellation
    when(authService.isTokenValid(userToken)).thenReturn(true);
    when(authService.getUserByToken(userToken)).thenReturn(usuario);
    when(linkAutoService.cancelParticipation(eventId, usuario)).thenReturn(true);
    
    ResponseEntity<Void> result = linkAutoController.cancelParticipation(eventId, userToken);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(linkAutoService, times(1)).cancelParticipation(eventId, usuario);
    
    // Test not found scenario
    when(linkAutoService.cancelParticipation(2L, usuario)).thenReturn(false);
    ResponseEntity<Void> notFoundResult = linkAutoController.cancelParticipation(2L, userToken);
    assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
}

@Test
public void testGetEventParticipants() {
    Long eventId = 1L;
    Set<String> participants = new HashSet<>(Arrays.asList("user1", "user2", "user3"));
    
    when(linkAutoService.getEventParticipants(eventId)).thenReturn(participants);
    
    ResponseEntity<Set<String>> result = linkAutoController.getEventParticipants(eventId);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(3, result.getBody().size());
    
    // Test not found scenario
    when(linkAutoService.getEventParticipants(2L)).thenReturn(null);
    ResponseEntity<Set<String>> notFoundResult = linkAutoController.getEventParticipants(2L);
    assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
}

@Test
public void testGetUserEvents() {
    String username = "testUser";
    List<Event> events = new ArrayList<>();
    User creator = usuario;
    
    Event event1 = new Event(1L, creator, "Event 1", "Description 1", "Location 1", 
                           1234567L, 1234568L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    Event event2 = new Event(2L, creator, "Event 2", "Description 2", "Location 2", 
                           1234569L, 1234570L, new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    
    events.add(event1);
    events.add(event2);
    
    when(linkAutoService.getEventsByUsername(username)).thenReturn(events);
    
    ResponseEntity<List<EventReturnerDTO>> result = linkAutoController.getUserEvents(username);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(2, result.getBody().size());
    assertEquals("Event 1", result.getBody().get(0).getTitle());
    assertEquals("Event 2", result.getBody().get(1).getTitle());
}

@Test
public void testUpdateEvent() {
    String userToken = "validToken";
    Long eventId = 1L;
    List<String> images = Arrays.asList("updated-img1.jpg", "updated-img2.jpg");
    EventDTO eventDTO = new EventDTO("Updated Event", "Updated Description", "Updated Location", 
                                  9876543L, 9876544L, images);
    
    // Test unauthorized scenario
    when(authService.isTokenValid("invalidToken")).thenReturn(false);
    ResponseEntity<EventReturnerDTO> unauthorizedResult = linkAutoController.updateEvent(eventId, "invalidToken", eventDTO);
    assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResult.getStatusCode());
    
    // Test successful update
    when(authService.isTokenValid(userToken)).thenReturn(true);
    when(authService.getUserByToken(userToken)).thenReturn(usuario);
    
    Event updatedEvent = new Event(eventId, usuario, "Updated Event", "Updated Description", "Updated Location", 
                                 9876543L, 9876544L, images, new HashSet<>(), new ArrayList<>());
    
    when(linkAutoService.updateEvent(eventId, eventDTO, usuario)).thenReturn(Optional.of(updatedEvent));
    
    ResponseEntity<EventReturnerDTO> result = linkAutoController.updateEvent(eventId, userToken, eventDTO);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals("Updated Event", result.getBody().getTitle());
    assertEquals("Updated Description", result.getBody().getDescription());
    assertEquals(eventId, result.getBody().getId());
    
    // Test not found scenario
    when(linkAutoService.updateEvent(2L, eventDTO, usuario)).thenReturn(Optional.empty());
    ResponseEntity<EventReturnerDTO> notFoundResult = linkAutoController.updateEvent(2L, userToken, eventDTO);
    assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
}

@Test
public void testParseEventToEventReturnerDTO() {
    // This is a test for the private helper method, we'll need to use reflection to test it
    // For this example, we'll test it indirectly through the public methods that use it
    
    User creator = usuario;
    Long eventId = 1L;
    List<String> images = Arrays.asList("img1.jpg", "img2.jpg");
    Set<String> participants = new HashSet<>(Arrays.asList("user1", "user2"));
    List<Comment> comments = new ArrayList<>();
    
    Comment comment1 = new Comment("Test comment 1", creator, null, 1234567L);
    comment1.setId(10L);
    Comment comment2 = new Comment("Test comment 2", creator, null, 1234568L);
    comment2.setId(11L);
    comments.add(comment1);
    comments.add(comment2);
    
    Event event = new Event(eventId, creator, "Test Event", "Test Description", "Test Location", 
                          1234567L, 1234568L, images, participants, comments);
    
    // We need to set the event reference for each comment
    comment1.setEvent(event);
    comment2.setEvent(event);
    
    when(linkAutoService.getEventById(eventId)).thenReturn(Optional.of(event));
    
    ResponseEntity<EventReturnerDTO> result = linkAutoController.getEventById(eventId);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(eventId, result.getBody().getId());
    assertEquals("Test Event", result.getBody().getTitle());
    assertEquals("Test Description", result.getBody().getDescription());
    assertEquals("Test Location", result.getBody().getLocation());
    assertEquals(1234567L, result.getBody().getStartDate());
    assertEquals(1234568L, result.getBody().getEndDate());
    assertEquals(2, result.getBody().getImages().size());
    assertEquals(2, result.getBody().getParticipants().size());
    assertEquals(2, result.getBody().getComment_ids().size());
}
    
    @Test
    public void testBanUser_Unauthorized() {
        // Datos de prueba
        String username = "testUser";
        boolean banStatus = true;
        String userToken = "invalidToken";
    
        // Mock del servicio
        when(authService.isTokenValid(userToken)).thenReturn(false);
    
        // Llamar al método del controlador
        ResponseEntity<Void> response = linkAutoController.banUser(username, banStatus, userToken);
    
        // Verificar el resultado
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService, never()).getUserByToken(anyString());
        verify(authService, never()).getUserByUsername(anyString());
        verify(authService, never()).banUser(anyString(), anyBoolean());
    }
    
    @Test
    public void testBanUser_Forbidden() {
        // Datos de prueba
        String username = "testUser";
        boolean banStatus = true;
        String userToken = "validToken";
    
        // Crear un usuario no administrador
        User nonAdminUser = new User("nonAdmin", "Non Admin Name", "nonAdminPic", "nonAdminEmail",
                new ArrayList<>(), 123456L, User.Gender.MALE, "nonAdminLocation", "nonAdminPassword",
                "nonAdminDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        nonAdminUser.setRole(Role.USER);
    
        // Mock del servicio
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(nonAdminUser);
    
        // Llamar al método del controlador
        ResponseEntity<Void> response = linkAutoController.banUser(username, banStatus, userToken);
    
        // Verificar el resultado
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService).getUserByToken(userToken);
        verify(authService, never()).getUserByUsername(anyString());
        verify(authService, never()).banUser(anyString(), anyBoolean());
    }
    
    @Test
    public void testBanUser_UserNotFound() {
        // Datos de prueba
        String username = "nonExistentUser";
        boolean banStatus = true;
        String userToken = "validToken";
    
        // Crear un usuario administrador
        User adminUser = new User("admin", "Admin Name", "adminPic", "adminEmail",
                new ArrayList<>(), 123456L, User.Gender.MALE, "adminLocation", "adminPassword",
                "adminDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        adminUser.setRole(Role.ADMIN);
    
        // Mock del servicio
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(adminUser);
        when(authService.getUserByUsername(username)).thenReturn(null);
    
        // Llamar al método del controlador
        ResponseEntity<Void> response = linkAutoController.banUser(username, banStatus, userToken);
    
        // Verificar el resultado
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService).getUserByToken(userToken);
        verify(authService).getUserByUsername(username);
        verify(authService, never()).banUser(anyString(), anyBoolean());
    }
    
    @Test
    public void testBanUser_InternalServerError() {
        // Datos de prueba
        String username = "testUser";
        boolean banStatus = true;
        String userToken = "validToken";
    
        // Crear usuarios de prueba
        User adminUser = new User("admin", "Admin Name", "adminPic", "adminEmail",
                new ArrayList<>(), 123456L, User.Gender.MALE, "adminLocation", "adminPassword",
                "adminDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        adminUser.setRole(Role.ADMIN);
    
        User targetUser = new User(username, "Target Name", "targetPic", "targetEmail",
                new ArrayList<>(), 123456L, User.Gender.FEMALE, "targetLocation", "targetPassword",
                "targetDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
    
        // Mock del servicio
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(adminUser);
        when(authService.getUserByUsername(username)).thenReturn(targetUser);
        when(authService.banUser(username, banStatus)).thenReturn(false);
    
        // Llamar al método del controlador
        ResponseEntity<Void> response = linkAutoController.banUser(username, banStatus, userToken);
    
        // Verificar el resultado
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService).getUserByToken(userToken);
        verify(authService).getUserByUsername(username);
        verify(authService).banUser(username, banStatus);
    }
    
    @Test
    public void testReportUser() {
        String userToken = "validToken";
        String username = "reportedUser";

        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<Void> invalidTokenResponse = linkAutoController.reportUser(username, "invalidToken");
        assertEquals(HttpStatus.UNAUTHORIZED, invalidTokenResponse.getStatusCode());

        // Case 2: Reported user does not exist
        when(authService.isTokenValid(userToken)).thenReturn(true);
        User reportingUser = new User("reportingUser", "name", "pic", "email", new ArrayList<>(), 123L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(authService.getUserByToken(userToken)).thenReturn(reportingUser);
        when(authService.getUserByUsername(username)).thenReturn(null);
        ResponseEntity<Void> notFoundResponse = linkAutoController.reportUser(username, userToken);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        // Case 3: Report successful
        User reportedUser = new User(username, "name", "pic", "email", new ArrayList<>(), 123L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(authService.getUserByUsername(username)).thenReturn(reportedUser);
        when(linkAutoService.reportUser(reportingUser, reportedUser)).thenReturn(true);
        ResponseEntity<Void> successResponse = linkAutoController.reportUser(username, userToken);
        assertEquals(HttpStatus.OK, successResponse.getStatusCode());

        // Case 4: Report failed (internal error)
        when(linkAutoService.reportUser(reportingUser, reportedUser)).thenReturn(false);
        ResponseEntity<Void> errorResponse = linkAutoController.reportUser(username, userToken);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.getStatusCode());
    }

    @Test
    public void testDeleteReport() {
        String userToken = "validToken";
        String usernameOwningTheReport = "usernameOwningTheReport";
        String usernameToDelete = "usernameToDelete";

        // Case 1: Invalid token
        when(authService.isTokenValid("invalidToken")).thenReturn(false);
        ResponseEntity<Void> invalidTokenResponse = linkAutoController.deleteReport(usernameToDelete, usernameOwningTheReport, "invalidToken");
        assertEquals(HttpStatus.UNAUTHORIZED, invalidTokenResponse.getStatusCode());

        // Case 2: Reported user not found (null)
        when(authService.isTokenValid(userToken)).thenReturn(true); 
        User adminUser = new User("admin", "name", "pic", "email", new ArrayList<>(), 123L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        adminUser.setRole(Role.ADMIN);
        when(authService.getUserByToken(userToken)).thenReturn(adminUser);
        when(authService.getUserByUsername(usernameOwningTheReport)).thenReturn(null);
        when(authService.getUserByUsername(usernameToDelete)).thenReturn(null);
        ResponseEntity<Void> notFoundResponse = linkAutoController.deleteReport(usernameToDelete, usernameOwningTheReport, userToken);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        // Case 3: Delete report successful
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(adminUser);
        User userToDelete = new User("usernameToDelete", "name", "pic", "email", new ArrayList<>(), 123L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User userOwningTheReport = new User("usernameOwningTheReport", "name", "pic", "email", new ArrayList<>(), 123L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        userOwningTheReport.setReporters(userToDelete);
        when(authService.getUserByUsername(userToDelete.getUsername())).thenReturn(userToDelete);
        when(authService.getUserByUsername(userOwningTheReport.getUsername())).thenReturn(userOwningTheReport);
        when(linkAutoService.deleteReport(userToDelete.getUsername(), userOwningTheReport.getUsername())).thenReturn(true);
        ResponseEntity<Void> successResponse = linkAutoController.deleteReport(userToDelete.getUsername(), userOwningTheReport.getUsername(), userToken);
        assertEquals(HttpStatus.OK, successResponse.getStatusCode());

        // Case 4: Delete report failed (internal error)
        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(adminUser);
        when(authService.getUserByUsername(userToDelete.getUsername())).thenReturn(userToDelete);
        when(authService.getUserByUsername(userOwningTheReport.getUsername())).thenReturn(userOwningTheReport);
        when(linkAutoService.deleteReport(userToDelete.getUsername(), userOwningTheReport.getUsername())).thenReturn(false);
        ResponseEntity<Void> errorResponse = linkAutoController.deleteReport(userToDelete.getUsername(), userOwningTheReport.getUsername(), userToken);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.getStatusCode());
    }

    @Test
    public void testParseUserToUserReturnerDTO_WithReporters() {
        // Arrange
        User reporter1 = new User("reporter1", "Reporter One", "pic1", "email1", new ArrayList<>(), 111L, Gender.FEMALE, "loc1", "pass1", "desc1", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User reporter2 = new User("reporter2", "Reporter Two", "pic2", "email2", new ArrayList<>(), 222L, Gender.MALE, "loc2", "pass2", "desc2", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User mainUser = new User("mainUser", "Main User", "mainPic", "mainEmail", new ArrayList<>(), 333L, Gender.OTHER, "mainLoc", "mainPass", "mainDesc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        mainUser.setReporters(reporter1);
        mainUser.setReporters(reporter2);

        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = LinkAutoController.class.getDeclaredMethod("parseUserToUserReturnerDTO", User.class);
            method.setAccessible(true);
            UserReturnerDTO dto = (UserReturnerDTO) method.invoke(linkAutoController, mainUser);

            assertNotNull(dto);
            assertEquals("mainUser", dto.getUsername());
            assertEquals("Main User", dto.getName());
            assertEquals("mainEmail", dto.getEmail());
            assertEquals("mainPic", dto.getProfilePicture());
            assertEquals("mainLoc", dto.getLocation());
            assertEquals("mainDesc", dto.getDescription());
            assertEquals(mainUser.getRole().toString(), dto.getRole());
            assertEquals(mainUser.getBirthDate(), dto.getBirthDate());
            assertEquals(mainUser.getGender().toString(), dto.getGender());
            assertNotNull(dto.getReporters());
            assertEquals(2, dto.getReporters().size());
            // Check that both reporters are present in the DTO set
            boolean foundReporter1 = dto.getReporters().stream().anyMatch(r -> r.getUsername().equals("reporter1"));
            boolean foundReporter2 = dto.getReporters().stream().anyMatch(r -> r.getUsername().equals("reporter2"));
            assertEquals(true, foundReporter1);
            assertEquals(true, foundReporter2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @Test
    public void testVerifyUser_UnauthorizedToken() {
        String username = "johndoe";
        String userToken = "invalidToken";
        when(authService.isTokenValid(userToken)).thenReturn(false);

        ResponseEntity<Void> response = linkAutoController.verifyUser(username, userToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService, never()).getUserByToken(anyString());
        verify(authService, never()).getUserByUsername(anyString());
        verify(linkAutoService, never()).verifyUser(any(User.class));
    }

    @Test
    public void testVerifyUser_UnauthorizedNotAdminOrSelf() {
        String username = "johndoe";
        String userToken = "validToken";
        User notAdmin = new User("otheruser", "name", "pic", "email", new ArrayList<>(), 1L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        notAdmin.setRole(Role.USER);

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(notAdmin);

        ResponseEntity<Void> response = linkAutoController.verifyUser(username, userToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService).isTokenValid(userToken);
        verify(authService).getUserByToken(userToken);
        verify(authService, never()).getUserByUsername(anyString());
        verify(linkAutoService, never()).verifyUser(any(User.class));
    }

    @Test
    public void testVerifyUser_TargetUserNotFound() {
        String username = "johndoe";
        String userToken = "validToken";
        User admin = new User("admin", "name", "pic", "email", new ArrayList<>(), 1L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        admin.setRole(Role.ADMIN);

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(admin);
        when(authService.getUserByUsername(username)).thenReturn(null);

        ResponseEntity<Void> response = linkAutoController.verifyUser(username, userToken);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(authService).getUserByUsername(username);
        verify(linkAutoService, never()).verifyUser(any(User.class));
    }


    @Test
    public void testVerifyUser_Success_Admin() {
        String username = "johndoe";
        String userToken = "validToken";
        User admin = new User("admin", "name", "pic", "email", new ArrayList<>(), 1L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        admin.setRole(Role.ADMIN);
        User targetUser = new User(username, "name", "pic", "email", new ArrayList<>(), 2L, Gender.FEMALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(admin);
        when(authService.getUserByUsername(username)).thenReturn(targetUser);
        when(linkAutoService.verifyUser(targetUser)).thenReturn(true);

        ResponseEntity<Void> response = linkAutoController.verifyUser(username, userToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(linkAutoService).verifyUser(targetUser);
    }

    @Test
    public void testVerifyUser_Forbidden() {
        String username = "johndoe";
        String userToken = "validToken";
        User admin = new User("admin", "name", "pic", "email", new ArrayList<>(), 1L, Gender.MALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        admin.setRole(Role.ADMIN);
        User targetUser = new User(username, "name", "pic", "email", new ArrayList<>(), 2L, Gender.FEMALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(admin);
        when(authService.getUserByUsername(username)).thenReturn(targetUser);
        when(linkAutoService.verifyUser(targetUser)).thenReturn(false);

        ResponseEntity<Void> response = linkAutoController.verifyUser(username, userToken);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(linkAutoService).verifyUser(targetUser);
    }

    @Test
    public void testVerifyUser_Success_Self() {
        String username = "johndoe";
        String userToken = "validToken";
        User self = new User(username, "name", "pic", "email", new ArrayList<>(), 2L, Gender.FEMALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        self.setRole(Role.USER);

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(self);
        when(authService.getUserByUsername(username)).thenReturn(self);
        when(linkAutoService.verifyUser(self)).thenReturn(true);

        ResponseEntity<Void> response = linkAutoController.verifyUser(username, userToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(linkAutoService).verifyUser(self);
    }

    @Test
    public void testIsUserVerified_UserFound() {
        String username = "johndoe";
        User user = new User(username, "name", "pic", "email", new ArrayList<>(), 2L, Gender.FEMALE, "loc", "pass", "desc", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        user.setIsVerified(true);

        when(authService.getUserByUsername(username)).thenReturn(user);

        ResponseEntity<Boolean> response = linkAutoController.isUserVerified(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    public void testIsUserVerified_UserNotFound() {
        String username = "nonexistent";
        when(authService.getUserByUsername(username)).thenReturn(null);

        ResponseEntity<Boolean> response = linkAutoController.isUserVerified(username);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
