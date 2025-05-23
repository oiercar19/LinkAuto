package com.linkauto.client.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.linkauto.client.data.Comment;
import com.linkauto.client.data.CommentCreator;
import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
import com.linkauto.client.data.UpdateUser;
import com.linkauto.client.data.User;
import com.linkauto.client.data.Event;
import com.linkauto.client.data.EventCreator;

class ClientServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ClientServiceProxy clientServiceProxy;

    private final String API_BASE_URL = "http://localhost:8080";
    private final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(clientServiceProxy, "apiBaseUrl", API_BASE_URL);
    }

    @Test
    void testRegister_Success() {
        // Create User record with constructor parameters
        User user = new User("testuser", "USER", false,"test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        String url = API_BASE_URL + "/auth/register";
        when(restTemplate.postForObject(eq(url), eq(user), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.register(user));
        verify(restTemplate).postForObject(url, user, Void.class);
    }

    @Test
    void testRegister_BadRequest() {
        User user = new User("testuser", "USER", false,"test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        String url = API_BASE_URL + "/auth/register";
        
        when(restTemplate.postForObject(eq(url), eq(user), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.register(user));
        assertEquals("Registration failed: Invalid user data", exception.getMessage());
    }

    @Test
    void testLogin_Success() {
        Credentials credentials = new Credentials("testuser", "password");
        String url = API_BASE_URL + "/auth/login";
        
        when(restTemplate.postForObject(eq(url), eq(credentials), eq(String.class)))
            .thenReturn(TOKEN);
        
        String result = clientServiceProxy.login(credentials);
        assertEquals(TOKEN, result);
    }

    @Test
    void testLogin_Unauthorized() {
        Credentials credentials = new Credentials("testuser", "password");
        String url = API_BASE_URL + "/auth/login";
        
        when(restTemplate.postForObject(eq(url), eq(credentials), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.login(credentials));
        assertEquals("Login failed: Invalid credentials", exception.getMessage());
    }

    @Test
    void testLogout_Success() {
        String url = String.format("%s/auth/logout?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(TOKEN), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.logout(TOKEN));
    }

    @Test
    void testLogout_Unauthorized() {
        String url = String.format("%s/auth/logout?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(TOKEN), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.logout(TOKEN));
        assertEquals("Logout failed: Invalid token", exception.getMessage());
    }

    @Test
    void testGetUserProfile_Success() {
        User user = new User("testuser", "USER", false,"test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.getForObject(url, User.class)).thenReturn(user);
        
        User result = clientServiceProxy.getUserProfile(TOKEN);
        assertEquals(user, result);
    }

    @Test
    void testGetUserProfile_Unauthorized() {
        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.getForObject(url, User.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserProfile(TOKEN));
        assertEquals("Unauthorized: Invalid username", exception.getMessage());
    }

    @Test
    void testGetUserProfile_NotFound() {
        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.getForObject(url, User.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserProfile(TOKEN));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateProfile_Success() {
        UpdateUser user = new UpdateUser("testuser","test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");

        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        doNothing().when(restTemplate).put(url, user, Void.class);
        
        assertDoesNotThrow(() -> clientServiceProxy.updateProfile(TOKEN, user));
    }

    @Test
    void testUpdateProfile_Unauthorized() {
        UpdateUser user = new UpdateUser("testuser","test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");

        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).put(url, user, Void.class);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.updateProfile(TOKEN, user));
        assertEquals("Unauthorized: Invalid username", exception.getMessage());
    }

    @Test
    void testCreatePost_Success() {
        PostCreator post = new PostCreator("Test post content", new ArrayList<>());
        String url = String.format("%s/api/posts?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(post), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.createPost(TOKEN, post));
    }

    @Test
    void testCreatePost_Unauthorized() {
        PostCreator post = new PostCreator("Test post content", new ArrayList<>());
        String url = String.format("%s/api/posts?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(post), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.createPost(TOKEN, post));
        assertEquals("Logout failed: Invalid token", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetFeed_Success() {
        Post post1 = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(1L, "username2", "content2", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        List<Post> posts = Arrays.asList(post1, post2);
        String url = String.format("%s/api/posts", API_BASE_URL);
        
        ResponseEntity<List<Post>> responseEntity = new ResponseEntity<>(posts, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        List<Post> result = clientServiceProxy.getFeed();
        assertEquals(posts, result);
    }

    @Test
    void testGetPostById_Success() {
        Post post = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1);
        
        when(restTemplate.getForObject(url, Post.class)).thenReturn(post);
        
        Post result = clientServiceProxy.getPostById(1L);
        assertEquals(post, result);
    }

    @Test

            void testGetPostById_NotFound() {
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1);
        
        when(restTemplate.getForObject(url, Post.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getPostById(1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testDeletePost_Success() {
        String url = String.format("%s/api/posts/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        doNothing().when(restTemplate).delete(url);
        
        assertDoesNotThrow(() -> clientServiceProxy.deletePost(TOKEN, 1L));
    }

    @Test
    void testDeletePost_Unauthorized() {
        String url = String.format("%s/api/posts/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deletePost(TOKEN, 1L));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testGetUserByUsername_Success() {
        User user = new User("testuser", "USER", false,"test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        String url = String.format("%s/api/user/%s", API_BASE_URL, "testuser");
        
        when(restTemplate.getForObject(url, User.class)).thenReturn(user);
        
        User result = clientServiceProxy.getUserByUsername("testuser");
        assertEquals(user, result);
    }

    @Test
    void testGetUserByUsername_NotFound() {
        String url = String.format("%s/api/user/%s", API_BASE_URL, "nonexistent");
        
        when(restTemplate.getForObject(url, User.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserByUsername("nonexistent"));
        assertEquals("User not found", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserFollowers_Success() {
        User user1 = new User("follower", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        User user2 = new User("follower2", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        List<User> followers = Arrays.asList(user1, user2);
        String url = String.format("%s/api/user/%s/followers", API_BASE_URL, "testuser");
        
        ResponseEntity<List<User>> responseEntity = new ResponseEntity<>(followers, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        List<User> result = clientServiceProxy.getUserFollowers("testuser");
        assertEquals(followers, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserFollowing_Success() {
        User user1 = new User("following1", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        User user2 = new User("following2", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        List<User> following = Arrays.asList(user1, user2);
        String url = String.format("%s/api/user/%s/following", API_BASE_URL, "testuser");
        
        ResponseEntity<List<User>> responseEntity = new ResponseEntity<>(following, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        List<User> result = clientServiceProxy.getUserFollowing("testuser");
        assertEquals(following, result);
    }

    @Test
    void testFollowUser_Success() {
        String url = String.format("%s/api/user/%s/follow?userToken=%s", API_BASE_URL, "userToFollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.followUser(TOKEN, "userToFollow"));
    }

    @Test
    void testUnfollowUser_Success() {
        String url = String.format("%s/api/user/%s/unfollow?userToken=%s", API_BASE_URL, "userToUnfollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.unfollowUser(TOKEN, "userToUnfollow"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserPosts_Success() {
        Post post1 = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(2L, "username2", "content2", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        List<Post> posts = Arrays.asList(post1, post2);
        String url = String.format("%s/api/user/%s/posts", API_BASE_URL, "testuser");
        
        ResponseEntity<List<Post>> responseEntity = new ResponseEntity<>(posts, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        List<Post> result = clientServiceProxy.getUserPosts("testuser");
        assertEquals(posts, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetCommentsByPostId_Success() {
        Comment comment1 = new Comment(1L, "texto", "testuser", 1L, 43456L);
        Comment comment2 = new Comment(2L, "texto", "testuser", 2L, 43456L);
        List<Comment> comments = Arrays.asList(comment1, comment2);
        String url = String.format("%s/api/post/%d/comments", API_BASE_URL, 1L);
        
        ResponseEntity<List<Comment>> responseEntity = new ResponseEntity<>(comments, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        List<Comment> result = clientServiceProxy.getCommentsByPostId(1L);
        assertEquals(comments, result);
    }

    @Test
    void testCommentPost_Success() {
        CommentCreator comment = new CommentCreator("Test comment content");
        String url = String.format("%s/api/user/%d/comment?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(comment), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.commentPost(TOKEN, 1L, comment));
    }

    @Test
    void testLikePost_Success() {
        String url = String.format("%s/api/user/%d/like?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.likePost(TOKEN, 1L));
    }

    @Test
    void testUnlikePost_Success() {
        String url = String.format("%s/api/user/%d/unlike?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.unlikePost(TOKEN, 1L));
    }

    @Test
    void testSharePost_Success() {
        Post post = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1L);
        
        when(restTemplate.getForObject(url, Post.class)).thenReturn(post);
        
        Post result = clientServiceProxy.sharePost(1L);
        assertEquals(post, result);
    }

    @Test
    void testSharePost_NotFound() {
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1L);
        
        when(restTemplate.getForObject(url, Post.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.sharePost(1L));
        assertEquals("Publicación no encontrada", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAllUsers_Success() {
        User user1 = new User("testuser1", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);
        User user2 = new User("testuser2", "USER", false, "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);


        List<User> users = Arrays.asList(user1, user2);
        String url = String.format("%s/api/users", API_BASE_URL);
        
        ResponseEntity<List<User>> responseEntity = new ResponseEntity<>(users, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        List<User> result = clientServiceProxy.getAllUsers();
        assertEquals(users, result);
    }

    @Test
    void testDeleteUser_Success() {
        String url = String.format("%s/api/user/%s?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doNothing().when(restTemplate).delete(url);
        
        assertDoesNotThrow(() -> clientServiceProxy.deleteUser(TOKEN, "testuser"));
    }

    @Test
    void testDeleteUser_Unauthorized() {
        String url = String.format("%s/api/user/%s?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteUser(TOKEN, "testuser"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testPromoteToAdmin_Success() {
        String url = String.format("%s/api/user/%s/role/admin?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doNothing().when(restTemplate).put(eq(url), isNull());
        
        assertDoesNotThrow(() -> clientServiceProxy.promoteToAdmin(TOKEN, "testuser"));
    }

    @Test
    void testPromoteToAdmin_Forbidden() {
        String url = String.format("%s/api/user/%s/role/admin?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.promoteToAdmin(TOKEN, "testuser"));
        assertEquals("Forbidden: You do not have permission to promote this user", exception.getMessage());
    }

    @Test
    void testDemoteToUser_Success() {
        String url = String.format("%s/api/user/%s/role/user?userToken=%s", API_BASE_URL, "adminuser", TOKEN);
        
        doNothing().when(restTemplate).put(eq(url), isNull());
        
        assertDoesNotThrow(() -> clientServiceProxy.demoteToUser(TOKEN, "adminuser"));
    }

    @Test
    void testDemoteToUser_Forbidden() {
        String url = String.format("%s/api/user/%s/role/user?userToken=%s", API_BASE_URL, "adminuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.demoteToUser(TOKEN, "adminuser"));
        assertEquals("Forbidden: You do not have permission to demote this admin", exception.getMessage());
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        User user = new User("testuser", "USER", false,"test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        String url = API_BASE_URL + "/auth/register";
        
        when(restTemplate.postForObject(eq(url), eq(user), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.register(user));
        assertEquals("Registration failed: Username or email already exists", exception.getMessage());
    }

    @Test
    void testRegister_OtherError() {
        User user = new User("testuser", "USER", false,"test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description", new HashSet<>(), false);

        String url = API_BASE_URL + "/auth/register";
        
        when(restTemplate.postForObject(eq(url), eq(user), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.register(user));
        assertEquals("Registration failed: Server Error", exception.getMessage());
    }

    // Login - Additional error case
    @Test
    void testLogin_OtherError() {
        Credentials credentials = new Credentials("testuser", "password");
        String url = API_BASE_URL + "/auth/login";
        
        when(restTemplate.postForObject(eq(url), eq(credentials), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.login(credentials));
        assertEquals("Login failed: Server Error", exception.getMessage());
    }

    // Logout - Additional error case
    @Test
    void testLogout_OtherError() {
        String url = String.format("%s/auth/logout?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(TOKEN), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.logout(TOKEN));
        assertEquals("Logout failed: Server Error", exception.getMessage());
    }

    // Get User Profile - Additional error case
    @Test
    void testGetUserProfile_OtherError() {
        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.getForObject(url, User.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserProfile(TOKEN));
        assertEquals("Failed to get user profile: Server Error", exception.getMessage());
    }

    // Update Profile - Additional error case
    @Test
    void testUpdateProfile_NotFound() {
        UpdateUser user = new UpdateUser("testuser","test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");

        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).put(url, user, Void.class);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.updateProfile(TOKEN, user));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateProfile_OtherError() {
        UpdateUser user = new UpdateUser("testuser","test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");

        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).put(url, user, Void.class);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.updateProfile(TOKEN, user));
        assertEquals("Failed to update profile: Server Error", exception.getMessage());
    }
    


    // Create Post - Missing tests for error handling
    @Test
    void testCreatePost_OtherError() {
        PostCreator post = new PostCreator("Test post content", new ArrayList<>());
        String url = String.format("%s/api/posts?userToken=%s", API_BASE_URL, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(post), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.createPost(TOKEN, post));
        assertEquals("Logout failed: Server Error", exception.getMessage());
    }

    // GetFeed - Missing tests for error handling
    @SuppressWarnings("unchecked")
    @Test
    void testGetFeed_Error() {
        String url = String.format("%s/api/posts", API_BASE_URL);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getFeed());
        assertEquals("Failed to get feed: Server Error", exception.getMessage());
    }

    // GetPostById - Missing additional error case
    @Test
    void testGetPostById_OtherError() {
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1);
        
        when(restTemplate.getForObject(url, Post.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getPostById(1L));
        assertEquals("Failed to get post: Server Error", exception.getMessage());
    }

    // DeletePost - Missing test cases
    @Test
    void testDeletePost_NotFound() {
        String url = String.format("%s/api/posts/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deletePost(TOKEN, 1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testDeletePost_OtherError() {
        String url = String.format("%s/api/posts/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deletePost(TOKEN, 1L));
        assertEquals("Failed to delete post: Server Error", exception.getMessage());
    }

    // GetUserByUsername - Missing additional error case
    @Test
    void testGetUserByUsername_OtherError() {
        String url = String.format("%s/api/user/%s", API_BASE_URL, "testuser");
        
        when(restTemplate.getForObject(url, User.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserByUsername("testuser"));
        assertEquals("Failed to get user: Server Error", exception.getMessage());
    }

    // GetUserFollowers - Missing test cases
    @SuppressWarnings("unchecked")
    @Test
    void testGetUserFollowers_NotFound() {
        String url = String.format("%s/api/user/%s/followers", API_BASE_URL, "testuser");
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserFollowers("testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserFollowers_OtherError() {
        String url = String.format("%s/api/user/%s/followers", API_BASE_URL, "testuser");
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserFollowers("testuser"));
        assertEquals("Failed to get user followers: Server Error", exception.getMessage());
    }

    // GetUserFollowing - Missing test cases
    @SuppressWarnings("unchecked")
    @Test
    void testGetUserFollowing_NotFound() {
        String url = String.format("%s/api/user/%s/following", API_BASE_URL, "testuser");
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserFollowing("testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserFollowing_OtherError() {
        String url = String.format("%s/api/user/%s/following", API_BASE_URL, "testuser");
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserFollowing("testuser"));
        assertEquals("Failed to get user following: Server Error", exception.getMessage());
    }

    // FollowUser - Missing test cases
    @Test
    void testFollowUser_Unauthorized() {
        String url = String.format("%s/api/user/%s/follow?userToken=%s", API_BASE_URL, "userToFollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.followUser(TOKEN, "userToFollow"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testFollowUser_NotFound() {
        String url = String.format("%s/api/user/%s/follow?userToken=%s", API_BASE_URL, "userToFollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.followUser(TOKEN, "userToFollow"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testFollowUser_OtherError() {
        String url = String.format("%s/api/user/%s/follow?userToken=%s", API_BASE_URL, "userToFollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.followUser(TOKEN, "userToFollow"));
        assertEquals("Failed to follow user: Server Error", exception.getMessage());
    }

    // UnfollowUser - Missing test cases
    @Test
    void testUnfollowUser_Unauthorized() {
        String url = String.format("%s/api/user/%s/unfollow?userToken=%s", API_BASE_URL, "userToUnfollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unfollowUser(TOKEN, "userToUnfollow"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testUnfollowUser_NotFound() {
        String url = String.format("%s/api/user/%s/unfollow?userToken=%s", API_BASE_URL, "userToUnfollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unfollowUser(TOKEN, "userToUnfollow"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUnfollowUser_OtherError() {
        String url = String.format("%s/api/user/%s/unfollow?userToken=%s", API_BASE_URL, "userToUnfollow", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unfollowUser(TOKEN, "userToUnfollow"));
        assertEquals("Failed to unfollow user: Server Error", exception.getMessage());
    }

    // GetUserPosts - Missing test cases
    @SuppressWarnings("unchecked")
    @Test
    void testGetUserPosts_NotFound() {
        String url = String.format("%s/api/user/%s/posts", API_BASE_URL, "testuser");
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserPosts("testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserPosts_OtherError() {
        String url = String.format("%s/api/user/%s/posts", API_BASE_URL, "testuser");
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserPosts("testuser"));
        assertEquals("Failed to get user posts: Server Error", exception.getMessage());
    }

    // GetCommentsByPostId - Missing test cases
    @SuppressWarnings("unchecked")
    @Test
    void testGetCommentsByPostId_NotFound() {
        String url = String.format("%s/api/post/%d/comments", API_BASE_URL, 1L);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getCommentsByPostId(1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetCommentsByPostId_OtherError() {
        String url = String.format("%s/api/post/%d/comments", API_BASE_URL, 1L);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getCommentsByPostId(1L));
        assertEquals("Failed to get comments: Server Error", exception.getMessage());
    }

    // CommentPost - Missing test cases
    @Test
    void testCommentPost_Unauthorized() {
        CommentCreator comment = new CommentCreator("Test comment content");
        String url = String.format("%s/api/user/%d/comment?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(comment), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.commentPost(TOKEN, 1L, comment));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testCommentPost_NotFound() {
        CommentCreator comment = new CommentCreator("Test comment content");
        String url = String.format("%s/api/user/%d/comment?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(comment), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.commentPost(TOKEN, 1L, comment));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testCommentPost_OtherError() {
        CommentCreator comment = new CommentCreator("Test comment content");
        String url = String.format("%s/api/user/%d/comment?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), eq(comment), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.commentPost(TOKEN, 1L, comment));
        assertEquals("Failed to comment on post: Server Error", exception.getMessage());
    }

    // LikePost - Missing test cases
    @Test
    void testLikePost_Unauthorized() {
        String url = String.format("%s/api/user/%d/like?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.likePost(TOKEN, 1L));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testLikePost_NotFound() {
        String url = String.format("%s/api/user/%d/like?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.likePost(TOKEN, 1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testLikePost_OtherError() {
        String url = String.format("%s/api/user/%d/like?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.likePost(TOKEN, 1L));
        assertEquals("Failed to like post: Server Error", exception.getMessage());
    }

    // UnlikePost - Missing test cases
    @Test
    void testUnlikePost_Unauthorized() {
        String url = String.format("%s/api/user/%d/unlike?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unlikePost(TOKEN, 1L));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testUnlikePost_NotFound() {
        String url = String.format("%s/api/user/%d/unlike?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unlikePost(TOKEN, 1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testUnlikePost_OtherError() {
        String url = String.format("%s/api/user/%d/unlike?userToken=%s", API_BASE_URL, 1L, TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unlikePost(TOKEN, 1L));
        assertEquals("Failed to unlike post: Server Error", exception.getMessage());
    }

    // SharePost - Additional error case
    @Test
    void testSharePost_OtherError() {
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1L);
        
        when(restTemplate.getForObject(url, Post.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.sharePost(1L));
        assertEquals("Error al compartir la publicación: Server Error", exception.getMessage());
    }

    // GetAllUsers - Missing test cases
    @SuppressWarnings("unchecked")
    @Test
    void testGetAllUsers_Unauthorized() {
        String url = String.format("%s/api/users", API_BASE_URL);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getAllUsers());
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAllUsers_Forbidden() {
        String url = String.format("%s/api/users", API_BASE_URL);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getAllUsers());
        assertEquals("Forbidden: You do not have permission to access this resource", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAllUsers_OtherError() {
        String url = String.format("%s/api/users", API_BASE_URL);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getAllUsers());
        assertEquals("Failed to fetch users: Server Error", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAllUsers_GenericException() {
        String url = String.format("%s/api/users", API_BASE_URL);
        
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("Network error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getAllUsers());
        assertEquals("Error inesperado al obtener usuarios", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Network error", exception.getCause().getMessage());
    }

    // DeleteUser - Missing test cases
    @Test
    void testDeleteUser_Forbidden() {
        String url = String.format("%s/api/user/%s?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteUser(TOKEN, "testuser"));
        assertEquals("Forbidden: You do not have permission to delete this user", exception.getMessage());
    }

    @Test
    void testDeleteUser_NotFound() {
        String url = String.format("%s/api/user/%s?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteUser(TOKEN, "testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteUser_OtherError() {
        String url = String.format("%s/api/user/%s?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).delete(url);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteUser(TOKEN, "testuser"));
        assertEquals("Failed to delete user: Server Error", exception.getMessage());
    }

    // PromoteToAdmin - Missing test cases
    @Test
    void testPromoteToAdmin_Unauthorized() {
        String url = String.format("%s/api/user/%s/role/admin?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.promoteToAdmin(TOKEN, "testuser"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testPromoteToAdmin_NotFound() {
        String url = String.format("%s/api/user/%s/role/admin?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.promoteToAdmin(TOKEN, "testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testPromoteToAdmin_OtherError() {
        String url = String.format("%s/api/user/%s/role/admin?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.promoteToAdmin(TOKEN, "testuser"));
        assertEquals("Failed to promote user to admin: Server Error", exception.getMessage());
    }

    // DemoteToUser - Missing test cases
    @Test
    void testDemoteToUser_Unauthorized() {
        String url = String.format("%s/api/user/%s/role/user?userToken=%s", API_BASE_URL, "adminuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.demoteToUser(TOKEN, "adminuser"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }
    @Test
    void testDemoteToUser_NotFound() {
        String url = String.format("%s/api/user/%s/role/user?userToken=%s", API_BASE_URL, "adminuser", TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(restTemplate).put(eq(url), isNull());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.demoteToUser(TOKEN, "adminuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDemoteToUser_OtherError() {
        String url = String.format("%s/api/user/%s/role/user?userToken=%s", API_BASE_URL, "adminuser", TOKEN);
        
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).put(eq(url), isNull());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.demoteToUser(TOKEN, "adminuser"));
        assertEquals("Failed to demote admin to user: Server Error", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
void testGetAllEvents_Success() {
    // Create test events
    Event event1 = new Event(1L, "user1", "Event 1", "Description 1", "Location 1", 
                            1683012000000L, 1683015600000L, new ArrayList<>(), 
                            new HashSet<>(), new ArrayList<>());
    Event event2 = new Event(2L, "user2", "Event 2", "Description 2", "Location 2", 
                            1683098400000L, 1683102000000L, new ArrayList<>(), 
                            new HashSet<>(), new ArrayList<>());
    List<Event> events = Arrays.asList(event1, event2);
    
    String url = String.format("%s/api/events", API_BASE_URL);
    
    // Configure mock response
    ResponseEntity<List<Event>> responseEntity = new ResponseEntity<>(events, HttpStatus.OK);
    
    when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)))
        .thenReturn(responseEntity);
    
    // Call method and verify result
    List<Event> result = clientServiceProxy.getAllEvents();
    assertEquals(events, result);
}

@SuppressWarnings("unchecked")
@Test
void testGetAllEvents_ServerError() {
    String url = String.format("%s/api/events", API_BASE_URL);
    
    when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
    
    RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getAllEvents());
    assertEquals("Failed to get events: Server Error", exception.getMessage());
}

@Test
void testGetEventById_Success() {
    // Create test event
    Event event = new Event(1L, "user1", "Event 1", "Description 1", "Location 1", 
                          1683012000000L, 1683015600000L, new ArrayList<>(), 
                          new HashSet<>(), new ArrayList<>());
    String url = String.format("%s/api/events/%d", API_BASE_URL, 1L);
    
    when(restTemplate.getForObject(url, Event.class)).thenReturn(event);
    
    Event result = clientServiceProxy.getEventById(1L);
    assertEquals(event, result);
}

@Test
void testGetEventById_NotFound() {
    String url = String.format("%s/api/events/%d", API_BASE_URL, 1L);
    
    when(restTemplate.getForObject(url, Event.class))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    
    RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getEventById(1L));
    assertEquals("Event not found", exception.getMessage());
}

@Test
void testCreateEvent_Success() {
    // Create test event creator
    List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
    EventCreator eventCreator = new EventCreator("Test Event", "Test Description", 
                                                "Test Location", 1683012000000L, 
                                                1683015600000L, images);
    String url = String.format("%s/api/events?userToken=%s", API_BASE_URL, TOKEN);
    
    when(restTemplate.postForObject(eq(url), eq(eventCreator), eq(Void.class))).thenReturn(null);
    
    assertDoesNotThrow(() -> clientServiceProxy.createEvent(TOKEN, eventCreator));
}

@Test
void testCreateEvent_Unauthorized() {
    // Create test event creator
    List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
    EventCreator eventCreator = new EventCreator("Test Event", "Test Description", 
                                                "Test Location", 1683012000000L, 
                                                1683015600000L, images);
    String url = String.format("%s/api/events?userToken=%s", API_BASE_URL, TOKEN);
    
    when(restTemplate.postForObject(eq(url), eq(eventCreator), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    
    RuntimeException exception = assertThrows(RuntimeException.class, 
                                            () -> clientServiceProxy.createEvent(TOKEN, eventCreator));
    assertEquals("Unauthorized: Invalid token", exception.getMessage());
}

@Test
void testCreateEvent_BadRequest() {
    // Create test event creator with invalid data
    List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
    EventCreator eventCreator = new EventCreator("", "Test Description", 
                                                "Test Location", 1683012000000L, 
                                                1683015600000L, images);
    String url = String.format("%s/api/events?userToken=%s", API_BASE_URL, TOKEN);
    
    when(restTemplate.postForObject(eq(url), eq(eventCreator), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
    
    RuntimeException exception = assertThrows(RuntimeException.class, 
                                            () -> clientServiceProxy.createEvent(TOKEN, eventCreator));
    assertEquals("Failed to create event: Invalid event data", exception.getMessage());
}

@Test
void testDeleteEvent_Success() {
    String url = String.format("%s/api/events/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    doNothing().when(restTemplate).delete(url);
    
    assertDoesNotThrow(() -> clientServiceProxy.deleteEvent(TOKEN, 1L));
}

@Test
void testDeleteEvent_Unauthorized() {
    String url = String.format("%s/api/events/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
        .when(restTemplate).delete(url);
    
    RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteEvent(TOKEN, 1L));
    assertEquals("Unauthorized: Invalid token", exception.getMessage());
}

@Test
void testDeleteEvent_Forbidden() {
    String url = String.format("%s/api/events/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
        .when(restTemplate).delete(url);
    
    RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteEvent(TOKEN, 1L));
    assertEquals("Forbidden: You do not have permission to delete this event", exception.getMessage());
}

@Test
void testDeleteEvent_NotFound() {
    String url = String.format("%s/api/events/%d?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
        .when(restTemplate).delete(url);
    
    RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteEvent(TOKEN, 1L));
    assertEquals("Event not found", exception.getMessage());
}

@Test
void testParticipateInEvent_Success() {
    String url = String.format("%s/api/events/%d/participate?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
    
    assertDoesNotThrow(() -> clientServiceProxy.participateInEvent(TOKEN, 1L));
}

@Test
void testParticipateInEvent_Unauthorized() {
    String url = String.format("%s/api/events/%d/participate?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    
    RuntimeException exception = assertThrows(RuntimeException.class, 
                                            () -> clientServiceProxy.participateInEvent(TOKEN, 1L));
    assertEquals("Unauthorized: Invalid token", exception.getMessage());
}

@Test
void testParticipateInEvent_NotFound() {
    String url = String.format("%s/api/events/%d/participate?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    
    RuntimeException exception = assertThrows(RuntimeException.class, 
                                            () -> clientServiceProxy.participateInEvent(TOKEN, 1L));
    assertEquals("Event not found", exception.getMessage());
}

@Test
void testCancelParticipation_Success() {
    String url = String.format("%s/api/events/%d/cancel?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
    
    assertDoesNotThrow(() -> clientServiceProxy.cancelParticipation(TOKEN, 1L));
}

@Test
void testCancelParticipation_Unauthorized() {
    String url = String.format("%s/api/events/%d/cancel?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    
    RuntimeException exception = assertThrows(RuntimeException.class, 
                                            () -> clientServiceProxy.cancelParticipation(TOKEN, 1L));
    assertEquals("Unauthorized: Invalid token", exception.getMessage());
}

@Test
void testCancelParticipation_NotFound() {
    String url = String.format("%s/api/events/%d/cancel?userToken=%s", API_BASE_URL, 1L, TOKEN);
    
    when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    
    RuntimeException exception = assertThrows(RuntimeException.class, 
                                            () -> clientServiceProxy.cancelParticipation(TOKEN, 1L));
    assertEquals("Event not found", exception.getMessage());
}
    @Test
    void testVerifyUser_Success() {
        String url = String.format("%s/api/user/%s/verify?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.verifyUser(TOKEN, "testuser"));
        verify(restTemplate).postForObject(url, null, Void.class);
    }
    
    @Test
    void testVerifyUser_Unauthorized() {
        String url = String.format("%s/api/user/%s/verify?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.verifyUser(TOKEN, "testuser"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }
    
    @Test
    void testVerifyUser_Forbidden() {
        String url = String.format("%s/api/user/%s/verify?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.verifyUser(TOKEN, "testuser"));
        assertEquals("Forbidden: You do not have permission to verify this user", exception.getMessage());
    }

    @Test
    void testVerifyUser_NotFound() {
        String url = String.format("%s/api/user/%s/verify?userToken=%s", API_BASE_URL, "nonexistent", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.verifyUser(TOKEN, "nonexistent"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testVerifyUser_OtherError() {
        String url = String.format("%s/api/user/%s/verify?userToken=%s", API_BASE_URL, "testuser", TOKEN);
        
        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.verifyUser(TOKEN, "testuser"));
        assertEquals("Failed to verify user: Server Error", exception.getMessage());
    }
    @Test
    void testIsUserVerified_Success() {
        String username = "testuser";
        String url = String.format("%s/api/user/%s/verify", API_BASE_URL, username);
        
        when(restTemplate.getForObject(url, Boolean.class)).thenReturn(true);
        
        Boolean result = clientServiceProxy.isUserVerified(username);
        assertTrue(result);
    }
    
    @Test
    void testIsUserVerified_UserNotFound() {
        String username = "nonexistent";
        String url = String.format("%s/api/user/%s/verify", API_BASE_URL, username);
        
        when(restTemplate.getForObject(url, Boolean.class))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.isUserVerified(username));
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void testIsUserVerified_Failure() {
        String username = "testuser";
        String url = String.format("%s/api/user/%s/verify", API_BASE_URL, username);
        
        when(restTemplate.getForObject(url, Boolean.class))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.isUserVerified(username));
        assertEquals("Failed to check user verification: INTERNAL_SERVER_ERROR", exception.getMessage());
    }
    
    @Test
    void testSavePost_Success() {
        String url = String.format("%s/api/post/%d/save?userToken=%s", API_BASE_URL, 1L, TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);

        assertDoesNotThrow(() -> clientServiceProxy.savePost(TOKEN, 1L));
    }

    @Test
    void testSavePost_Unauthorized() {
        String url = String.format("%s/api/post/%d/save?userToken=%s", API_BASE_URL, 1L, TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.savePost(TOKEN, 1L));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testSavePost_NotFound() {
        String url = String.format("%s/api/post/%d/save?userToken=%s", API_BASE_URL, 1L, TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.savePost(TOKEN, 1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testSavePost_OtherError() {
        String url = String.format("%s/api/post/%d/save?userToken=%s", API_BASE_URL, 1L, TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.savePost(TOKEN, 1L));
        assertEquals("Failed to save post: Server Error", exception.getMessage());
    }

    @Test
    void testUnsavePost_Success() {
        String url = String.format("%s/api/post/%d/unsave?userToken=%s", API_BASE_URL, 1L, TOKEN);

        doNothing().when(restTemplate).delete(url);

        assertDoesNotThrow(() -> clientServiceProxy.unsavePost(TOKEN, 1L));
    }

    @Test
    void testUnsavePost_Unauthorized() {
        String url = String.format("%s/api/post/%d/unsave?userToken=%s", API_BASE_URL, 1L, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).delete(url);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unsavePost(TOKEN, 1L));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testUnsavePost_NotFound() {
        String url = String.format("%s/api/post/%d/unsave?userToken=%s", API_BASE_URL, 1L, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).delete(url);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unsavePost(TOKEN, 1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void testUnsavePost_OtherError() {
        String url = String.format("%s/api/post/%d/unsave?userToken=%s", API_BASE_URL, 1L, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).delete(url);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.unsavePost(TOKEN, 1L));
        assertEquals("Failed to unsave post: Server Error", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserSavedPosts_Success() {
        Post post1 = new Post(1L, "username1", "content1", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(2L, "username2", "content2", 345345L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        List<Post> savedPosts = Arrays.asList(post1, post2);
        String url = String.format("%s/api/user/%s/savedPosts", API_BASE_URL, "testuser");

        ResponseEntity<List<Post>> responseEntity = new ResponseEntity<>(savedPosts, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        List<Post> result = clientServiceProxy.getUserSavedPosts("testuser");
        assertEquals(savedPosts, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserSavedPosts_NotFound() {
        String url = String.format("%s/api/user/%s/savedPosts", API_BASE_URL, "testuser");

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserSavedPosts("testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUserSavedPosts_OtherError() {
        String url = String.format("%s/api/user/%s/savedPosts", API_BASE_URL, "testuser");

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getUserSavedPosts("testuser"));
        assertEquals("Failed to get user saved posts: Server Error", exception.getMessage());
    }

    @Test
    void testDeleteReport_Success() {
        String url = String.format("%s/api/admin/%s/deleteReport/%s?userToken=%s", API_BASE_URL, "testuser", "testUser2", TOKEN);

        doNothing().when(restTemplate).delete(url);

        assertDoesNotThrow(() -> clientServiceProxy.deleteReport(TOKEN, "testuser", "testUser2"));
    }

    @Test
    void testDeleteReport_Unauthorized() {
        String url = String.format("%s/api/admin/%s/deleteReport/%s?userToken=%s", API_BASE_URL, "testUser", "testUser2", TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).delete(url);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteReport(TOKEN, "testUser2", "testUser"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testDeleteReport_NotFound() {
        String url = String.format("%s/api/admin/%s/deleteReport/%s?userToken=%s", API_BASE_URL, "testUser", "testUser2", TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).delete(url);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteReport(TOKEN, "testUser2", "testUser"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteReport_OtherError() {
        String url = String.format("%s/api/admin/%s/deleteReport/%s?userToken=%s", API_BASE_URL, "testUser", "testUser2", TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).delete(url);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.deleteReport(TOKEN, "testUser2", "testUser"));
        assertEquals("Failed to delete report: Server Error", exception.getMessage());
    }

    @Test
    void testReportUser_Success() {
        String url = String.format("%s/api/user/%s/report?userToken=%s", API_BASE_URL, "testuser", TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class))).thenReturn(null);

        assertDoesNotThrow(() -> clientServiceProxy.reportUser(TOKEN, "testuser"));
    }

    @Test
    void testReportUser_Unauthorized() {
        String url = String.format("%s/api/user/%s/report?userToken=%s", API_BASE_URL, "testuser", TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.reportUser(TOKEN, "testuser"));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testReportUser_NotFound() {
        String url = String.format("%s/api/user/%s/report?userToken=%s", API_BASE_URL, "testuser", TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.reportUser(TOKEN, "testuser"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testReportUser_OtherError() {
        String url = String.format("%s/api/user/%s/report?userToken=%s", API_BASE_URL, "testuser", TOKEN);

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.reportUser(TOKEN, "testuser"));
        assertEquals("Failed to report user: Server Error", exception.getMessage());
    }

    @Test
    void testBanUser_Success() {
        String url = String.format("%s/api/user/%s/ban?banStatus=%s&userToken=%s", API_BASE_URL, "testuser", true, TOKEN);

        doNothing().when(restTemplate).put(url, null);

        assertDoesNotThrow(() -> clientServiceProxy.banUser(TOKEN, "testuser", true));
    }

    @Test
    void testBanUser_Unauthorized() {
        String url = String.format("%s/api/user/%s/ban?banStatus=%s&userToken=%s", API_BASE_URL, "testuser", true, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
            .when(restTemplate).put(url, null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.banUser(TOKEN, "testuser", true));
        assertEquals("Unauthorized: Invalid token", exception.getMessage());
    }

    @Test
    void testBanUser_Forbidden() {
        String url = String.format("%s/api/user/%s/ban?banStatus=%s&userToken=%s", API_BASE_URL, "testuser", true, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
            .when(restTemplate).put(url, null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.banUser(TOKEN, "testuser", true));
        assertEquals("Forbidden: You do not have permission to ban this user", exception.getMessage());
    }

    @Test
    void testBanUser_NotFound() {
        String url = String.format("%s/api/user/%s/ban?banStatus=%s&userToken=%s", API_BASE_URL, "testuser", true, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
            .when(restTemplate).put(url, null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.banUser(TOKEN, "testuser", true));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testBanUser_OtherError() {
        String url = String.format("%s/api/user/%s/ban?banStatus=%s&userToken=%s", API_BASE_URL, "testuser", true, TOKEN);

        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"))
            .when(restTemplate).put(url, null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.banUser(TOKEN, "testuser", true));
        assertEquals("Failed to update ban status: Server Error", exception.getMessage());
    }
}