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
    void logout(String username);
    
    // User profile methods
    User getUserProfile(String username, int userId);
    void updateProfile(String username, User user);
    
    // Post methods
    void createPost(String username, Post post);
    void deletePost(String username, int postId);
    List<Post> getFeed(String username);
    List<Post> getUserPosts(String username, int userId);
    
    // Social interactions
    void followUser(String username, String followerId, int followeeId);
    void unfollowUser(String username, String followerId, int followeeId);
    void likePost(String username, String followerId, int postId);
    void unlikePost(String username, int postId);
    void commentOnPost(String username, String followerId, int postId, String comment);
    void deleteComment(String username, int postId, int commentId);

    // Search functionality
    List<User> searchUsers(String username, String query);
    List<Post> searchPosts(String username, String query);
}