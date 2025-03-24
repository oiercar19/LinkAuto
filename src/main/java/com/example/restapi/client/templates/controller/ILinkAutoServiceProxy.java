/**
 * Interface for the LinkAuto service proxy.
 * Defines the contract for communication with the backend service.
 */
package com.example.restapi.client.templates.controller;

import java.util.List;

import com.example.restapi.model.User;
import com.example.restapi.model.CredencialesDTO;
import com.example.restapi.model.Post;

public interface ILinkAutoServiceProxy {
    
    // Authentication methods
    void register(User user);
    User login(CredencialesDTO credentials);
    void logout(String token);
    
    // User profile methods
    User getUserProfile(String token, int userId);
    void updateProfile(String token, int userId, User user);
    
    // Post methods
    void createPost(String token, int userId, Post post);
    List<Post> getFeed(String token, int userId);
    List<Post> getUserPosts(String token, int userId);
    
    // Social interactions
    void followUser(String token, int followerId, int followeeId);
    void unfollowUser(String token, int followerId, int followeeId);
    void likePost(String token, int userId, int postId);
    void commentOnPost(String token, int userId, int postId, String comment);
    
    // Search functionality
    List<User> searchUsers(String token, String query);
    List<Post> searchPosts(String token, String query);
}