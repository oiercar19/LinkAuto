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
import com.linkauto.client.data.Comment;
import com.linkauto.client.data.CommentCreator;
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
    public Post getPostById(Long postId) {
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

    @Override
    public List<Post> getUserPosts(String username) {
        String url = String.format("%s/api/user/%s/posts", apiBaseUrl, username);
        
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
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user posts: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Post> getUserSavedPosts(String username) {
        String url = String.format("%s/api/user/%s/savedPosts", apiBaseUrl, username);
        
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
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to get user saved posts: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<Comment> getCommentsByPostId(long postId) {
        String url = String.format("%s/api/post/%d/comments", apiBaseUrl, postId);
        
        try {
            ResponseEntity<List<Comment>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Comment>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to get comments: " + e.getStatusText());
            }
        }
    }

    @Override
    public void commentPost(String token, Long postId, CommentCreator comment) {
        String url = String.format("%s/api/user/%d/comment?userToken=%s", apiBaseUrl, postId, token);
        
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
    public void likePost(String token, Long postId) {
        String url = String.format("%s/api/user/%d/like?userToken=%s", apiBaseUrl, postId, token);
        
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
    public void unlikePost(String token, Long postId) {
        String url = String.format("%s/api/user/%d/unlike?userToken=%s", apiBaseUrl, postId, token);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to unlike post: " + e.getStatusText());
            }
        }
    }

    @Override
    public Post sharePost(Long postId) {
        // Se construye la URL para llamar al endpoint que realiza el compartir.
        String url = String.format("%s/api/posts/%d", apiBaseUrl, postId);
    
        try {
            return restTemplate.getForObject(url,Post.class);
        } catch (HttpStatusCodeException e) {
            // Se manejan los distintos errores HTTP.
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("Publicación no encontrada");
                default -> throw new RuntimeException("Error al compartir la publicación: " + e.getStatusText());
            }
        }
    }

    @Override
    public List<User> getAllUsers() {
        String url = String.format("%s/api/users", apiBaseUrl);

        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            // Manejar errores HTTP específicos
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 403 -> throw new RuntimeException("Forbidden: You do not have permission to access this resource");
                default -> throw new RuntimeException("Failed to fetch users: " + e.getStatusText());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inesperado al obtener usuarios", e);
        }
    }
    
    @Override
    public void deleteUser(String token, String username) {
        String url = String.format("%s/api/user/%s?userToken=%s", apiBaseUrl, username, token);
        System.out.println("Sending DELETE request to URL: " + url); // Debug log
    
        try {
            restTemplate.delete(url);
            System.out.println("User deleted successfully: " + username); // Debug log
        } catch (HttpStatusCodeException e) {
            System.err.println("Error response from server: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()); // Debug log
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 403 -> throw new RuntimeException("Forbidden: You do not have permission to delete this user");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to delete user: " + e.getStatusText());
            }
        }
    }

    @Override
    public void promoteToAdmin(String token, String username) {
        String url = String.format("%s/api/user/%s/role/admin?userToken=%s", apiBaseUrl, username, token);
        
        try {
            restTemplate.put(url, null);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 403 -> throw new RuntimeException("Forbidden: You do not have permission to promote this user");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to promote user to admin: " + e.getStatusText());
            }
        }
    }

    @Override
    public void demoteToUser(String token, String username) {
        String url = String.format("%s/api/user/%s/role/user?userToken=%s", apiBaseUrl, username, token);
        
        try {
            restTemplate.put(url, null);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 403 -> throw new RuntimeException("Forbidden: You do not have permission to demote this admin");
                case 404 -> throw new RuntimeException("User not found");
                default -> throw new RuntimeException("Failed to demote admin to user: " + e.getStatusText());
            }
        }
    }

    @Override
    public void savePost(String token, Long postId) {
        String url = String.format("%s/api/post/%d/save?userToken=%s", apiBaseUrl, postId, token);
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to save post: " + e.getStatusText());
            }
        }
    }

    @Override
    public void unsavePost(String token, Long postId) {
        String url = String.format("%s/api/post/%d/unsave?userToken=%s", apiBaseUrl, postId, token);
        
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                case 404 -> throw new RuntimeException("Post not found");
                default -> throw new RuntimeException("Failed to unsave post: " + e.getStatusText());
            }
        }
    }
    
}