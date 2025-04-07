package com.linkauto.client.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
import com.linkauto.client.data.User;
import com.linkauto.client.data.Credentials;

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
    public String login(Credentials credentials) {
        String url = apiBaseUrl + "/auth/login";
            
        try {
            String token = restTemplate.postForObject(url, credentials, String.class);
            return token;
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Login failed: Invalid credentials");
                default -> throw new RuntimeException("Login failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public void logout(String userToken) {
        String url = String.format("%s/auth/logout?userToken=%s", apiBaseUrl, userToken);
        try {            
            restTemplate.postForObject(url, userToken, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Logout failed: Invalid token");
                default -> throw new RuntimeException("Logout failed: " + e.getStatusText());
            }
        }
    }

    @Override
    public User getUserProfile(String token) {
        String url = String.format("%s/api/user?userToken=%s", apiBaseUrl, token);
        
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
    public void updateProfile(String token, User user) {
        String url = String.format("%s/api/user?userToken=%s", apiBaseUrl, token);
        
        try {
            restTemplate.put(url, user, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid username");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to update profile: " + e.getStatusText());
            }
        }
    }

    @Override
    public void createPost(String token, PostCreator post) {
        String url = String.format("%s/api/posts?userToken=%s", apiBaseUrl, token);
        
        try {
            restTemplate.postForObject(url, post, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Logout failed: Invalid token");
                default -> throw new RuntimeException("Logout failed: " + e.getStatusText());
            }
        }
        
        
    }

    @Override
    public List<Post> getFeed() {
        String url = String.format("%s/api/posts", apiBaseUrl);
        
        try {
            ResponseEntity<List<Post>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to get feed: " + e.getStatusText());
        }
    }

    @Override
    public Post getPostById(int postId) {
        String url = String.format("%s/api/posts/%d", apiBaseUrl, postId);
        
        try {
            return restTemplate.getForObject(url, Post.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to get post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void deletePost(String token, Long postId) {
        String url = String.format("%s/api/posts/%d?userToken=%s", apiBaseUrl, postId, token);
        
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to delete post: " + e.getStatusText());
            }
        }
    }

    @Override
    public User getUserByUsername(String username){
        String url = String.format("%s/api/user/%s", apiBaseUrl, username);
        
        try {
            return restTemplate.getForObject(url, User.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<User> getUserFollowers(String username){
        String url = String.format("%s/api/user/%s/followers", apiBaseUrl, username);
        
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
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user followers: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<User> getUserFollowing(String username){
        String url = String.format("%s/api/user/%s/following", apiBaseUrl, username);
        
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
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user following: " + e.getStatusText());
            }
        }
    }

    @Override
    public void followUser(String token, String usernameToFollow) {
        String url = String.format("%s/api/user/%s/follow?userToken=%s", apiBaseUrl, usernameToFollow, token);
        
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
    public void unfollowUser(String token, String usernameToUnfollow) {
        String url = String.format("%s/api/user/%s/unfollow?userToken=%s", apiBaseUrl, usernameToUnfollow, token);
        
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
}