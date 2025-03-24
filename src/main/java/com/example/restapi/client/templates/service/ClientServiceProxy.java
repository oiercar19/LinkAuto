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
            return restTemplate.postForObject(url, credentials, User.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Login failed: Invalid credentials");
                default -> throw new RuntimeException("Login failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public void logout(String token) {
        String url = String.format("%s/auth/logout?token=%s", apiBaseUrl, token);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Logout failed: Invalid token");
                default -> throw new RuntimeException("Logout failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public User getUserProfile(String token, int userId) {
        String url = String.format("%s/users/%d?token=%s", apiBaseUrl, userId, token);
        
        try {
            return restTemplate.getForObject(url, User.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user profile: " + e.getStatusText());
            }
        }
    }

    @Override
    public void updateProfile(String token, int userId, User user) {
        String url = String.format("%s/users/%d?token=%s", apiBaseUrl, userId, token);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<User> entity = new HttpEntity<>(user, headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to update profile: " + e.getStatusText());
            }
        }
    }

    @Override
    public void createPost(String token, int userId, Post post) {
        String url = String.format("%s/users/%d/posts?token=%s", apiBaseUrl, userId, token);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Post> entity = new HttpEntity<>(post, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                default -> throw new RuntimeException("Failed to create post: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> getFeed(String token, int userId) {
        String url = String.format("%s/users/%d/feed?token=%s", apiBaseUrl, userId, token);
        
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
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                default -> throw new RuntimeException("Failed to get feed: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> getUserPosts(String token, int userId) {
        String url = String.format("%s/users/%d/posts?token=%s", apiBaseUrl, userId, token);
        
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
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user posts: " + e.getStatusText());
            }
        }
    }

    @Override
    public void followUser(String token, int followerId, int followeeId) {
        String url = String.format("%s/users/%d/follow/%d?token=%s", apiBaseUrl, followerId, followeeId, token);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to follow user: " + e.getStatusText());
            }
        }
    }

    @Override
    public void unfollowUser(String token, int followerId, int followeeId) {
        String url = String.format("%s/users/%d/unfollow/%d?token=%s", apiBaseUrl, followerId, followeeId, token);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to unfollow user: " + e.getStatusText());
            }
        }
    }

    @Override
    public void likePost(String token, int userId, int postId) {
        String url = String.format("%s/posts/%d/like?token=%s&userId=%d", apiBaseUrl, postId, token, userId);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to like post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void commentOnPost(String token, int userId, int postId, String comment) {
        String url = String.format("%s/posts/%d/comment?token=%s&userId=%d", apiBaseUrl, postId, token, userId);
        
        try {
            restTemplate.postForObject(url, comment, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to comment on post: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<User> searchUsers(String token, String query) {
        String url = String.format("%s/search/users?token=%s&query=%s", apiBaseUrl, token, query);
        
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
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                default -> throw new RuntimeException("Failed to search users: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> searchPosts(String token, String query) {
        String url = String.format("%s/search/posts?token=%s&query=%s", apiBaseUrl, token, query);
        
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
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                default -> throw new RuntimeException("Failed to search posts: " + e.getStatusText());
            }
        }
    }
}