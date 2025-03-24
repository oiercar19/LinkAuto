/**
 * Implementation of the LinkAuto service proxy using RestTemplate.
 * Handles communication with the backend service.
 */
package com.example.restapi.client.templates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.model.CredencialesDTO;
import com.example.restapi.client.templates.controller.ILinkAutoServiceProxy;

@Service
public class ClientServiceProxy implements ILinkAutoServiceProxy {

    private final RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public ClientServiceProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public void register(User user) {
        String url = apiBaseUrl + "/auth/register";
        
        try {
            restTemplate.postForObject(url, user, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 400 -> throw new RuntimeException("Registration failed: Invalid user data");
                case 409 -> throw new RuntimeException("Registration failed: Username or email already exists");
                default -> throw new RuntimeException("Registration failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public User login(CredencialesDTO credentials) {
        String url = apiBaseUrl + "/auth/login";
            
        try {
            User user = restTemplate.postForObject(url, credentials, User.class);
            return user;
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Login failed: Invalid credentials");
                default -> throw new RuntimeException("Login failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public void logout(String username) {
        String url = String.format("%s/auth/logout", apiBaseUrl);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            String logoutUrl = url + "?username=" + username;
            restTemplate.postForObject(logoutUrl, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Logout failed: Invalid username");
                default -> throw new RuntimeException("Logout failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public User getUserProfile(String username, int userId) {
        String url = String.format("%s/api/users/%d?username=%s", apiBaseUrl, userId, username);
        
        try {
            return restTemplate.getForObject(url, User.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user profile: " + e.getStatusText());
            }
        }
    }

    @Override
    public void updateProfile(String username, User user) {
        String url = String.format("%s/api/users/%s?username=%s", apiBaseUrl, user.getUsername(), username);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<User> entity = new HttpEntity<>(user, headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to update profile: " + e.getStatusText());
            }
        }
    }

    @Override
    public void createPost(String username, Post post) {
        String url = String.format("%s/api/posts?username=%s", apiBaseUrl, username);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Post> entity = new HttpEntity<>(post, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, Post.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                default -> throw new RuntimeException("Failed to create post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void deletePost(String username, int postId) {
        String url = String.format("%s/api/posts/%d?username=%s", 
                apiBaseUrl, postId, username);
        
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 403 -> throw new RuntimeException("Forbidden: You can only delete your own posts");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to delete post: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> getFeed(String username) {
        String url = String.format("%s/api/posts?username=%s", apiBaseUrl, username);
        
        try {
            ResponseEntity<List<Post>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                default -> throw new RuntimeException("Failed to get feed: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> getUserPosts(String username, int userId) {
        String url = String.format("%s/api/users/%d/posts?username=%s", apiBaseUrl, userId, username);
        
        try {
            ResponseEntity<List<Post>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user posts: " + e.getStatusText());
            }
        }
    }

    @Override
    public void followUser(String username, String followerId, int followeeId) {
        String url = String.format("%s/api/users/%d/follow?username=%s", 
                apiBaseUrl, followeeId, username);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to follow user: " + e.getStatusText());
            }
        }
    }

    @Override
    public void unfollowUser(String username, String followerId, int followeeId) {
        String url = String.format("%s/api/users/%d/unfollow?username=%s", 
                apiBaseUrl, followeeId, username);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to unfollow user: " + e.getStatusText());
            }
        }
    }

    @Override
    public void likePost(String username, String followerId, int postId) {
        String url = String.format("%s/api/posts/%d/like?username=%s", 
                apiBaseUrl, postId, username);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to like post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void unlikePost(String username, int postId) {
        String url = String.format("%s/api/posts/%d/unlike?username=%s", 
                apiBaseUrl, postId, username);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to unlike post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void commentOnPost(String username, String followerId, int postId, String comment) {
        String url = String.format("%s/api/posts/%d/comment?username=%s", 
                apiBaseUrl, postId, username);
        
        try {
            restTemplate.postForObject(url, comment, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to comment on post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void deleteComment(String username, int postId, int commentId) {
        String url = String.format("%s/api/posts/%d/comments/%d?username=%s", 
                apiBaseUrl, postId, commentId, username);
        
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 403 -> throw new RuntimeException("Forbidden: You can only delete your own comments");
                case 404 -> throw new RuntimeException("Comment not found");
                default -> throw new RuntimeException("Failed to delete comment: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<User> searchUsers(String username, String query) {
        String url = String.format("%s/api/search/users?username=%s&query=%s", 
                apiBaseUrl, username, query);
        
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                default -> throw new RuntimeException("Failed to search users: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> searchPosts(String username, String query) {
        String url = String.format("%s/api/search/posts?username=%s&query=%s", 
                apiBaseUrl, username, query);
        
        try {
            ResponseEntity<List<Post>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                default -> throw new RuntimeException("Failed to search posts: " + e.getStatusText());
            }
        }
    }

}