/**
 * Interface for the LinkAuto service proxy.
 * Defines the contract for communication with the backend service.
 */
package com.linkauto.client.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.linkauto.client.data.Comment;
import com.linkauto.client.data.CommentCreator;
import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
import com.linkauto.client.data.User;
import com.linkauto.client.data.Event;
import com.linkauto.client.data.EventCreator;

@Service
public interface ILinkAutoServiceProxy {
    
    // Authentication methods
    void register(User user);
    String login(Credentials credentials);
    void logout(String username);
    
    // User profile methods
    User getUserProfile(String username);
    void updateProfile(String username, User user);
    
    // Post methods
    void createPost(String token, PostCreator post);
    void deletePost(String token, Long postId);
    List<Post> getFeed();
    Post getPostById(Long postId);

    // User methods
    User getUserByUsername(String username);
    List<User> getUserFollowers(String username);
    List<User> getUserFollowing(String username);
    void followUser(String token, String usernameToFollow);
    void unfollowUser(String token, String usernameToUnfollow);
    List<Post> getUserPosts(String username);
    
    // Comment and interaction methods
    List<Comment> getCommentsByPostId(long postId);
    void unlikePost(String token, Long postId);
    void likePost(String token, Long postId);
    void commentPost(String token, Long postId, CommentCreator comment);
    Post sharePost(Long postId);
    
    // Admin methods
    List<User> getAllUsers();
    void deleteUser(String token, String username);
    void banUser(String token, String username, boolean banStatus);
    void promoteToAdmin(String token, String username);
    void demoteToUser(String token, String username);
    
    // Event methods
    List<Event> getAllEvents();
    Event getEventById(Long eventId);
    void createEvent(String token, EventCreator event);
    void deleteEvent(String token, Long eventId);
    void participateInEvent(String token, Long eventId);
    void cancelParticipation(String token, Long eventId);
    void reportUser(String token, String username);
    void deleteReport(String token, String username);
    void verifyUser(String token, String username);
    Boolean isUserVerified(String username);
    List<Post> getUserSavedPosts(String username);
    void savePost(String token, Long postId);
    void unsavePost(String token, Long postId);

}