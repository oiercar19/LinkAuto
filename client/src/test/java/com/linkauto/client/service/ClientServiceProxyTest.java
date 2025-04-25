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
import com.linkauto.client.data.User;
import com.linkauto.client.service.ClientServiceProxy;

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
        User user = new User("testuser", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
        String url = API_BASE_URL + "/auth/register";
        when(restTemplate.postForObject(eq(url), eq(user), eq(Void.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> clientServiceProxy.register(user));
        verify(restTemplate).postForObject(url, user, Void.class);
    }

    @Test
    void testRegister_BadRequest() {
        User user = new User("testuser", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
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
        User user = new User("testuser", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
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
        User user = new User("testuser", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
        String url = String.format("%s/api/user?userToken=%s", API_BASE_URL, TOKEN);
        
        doNothing().when(restTemplate).put(url, user, Void.class);
        
        assertDoesNotThrow(() -> clientServiceProxy.updateProfile(TOKEN, user));
    }

    @Test
    void testUpdateProfile_Unauthorized() {
        User user = new User("testuser", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
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
        
        Post result = clientServiceProxy.getPostById(1);
        assertEquals(post, result);
    }

    @Test
    void testGetPostById_NotFound() {
        String url = String.format("%s/api/posts/%d", API_BASE_URL, 1);
        
        when(restTemplate.getForObject(url, Post.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientServiceProxy.getPostById(1));
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
        User user = new User("testuser", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
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

    @Test
    void testGetUserFollowers_Success() {
        User user1 = new User("follower", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
        User user2 = new User("follower2", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
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

    @Test
    void testGetUserFollowing_Success() {
        User user1 = new User("following1", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
        User user2 = new User("following2", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");

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

    @Test
    void testGetAllUsers_Success() {
        User user1 = new User("testuser1", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");
        User user2 = new User("testuser2", "MALE", "test", "profilePicture", "test@example.com", new ArrayList<>(), 1325413L, "MALE", "Bilbao", "1234", "description");

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
}