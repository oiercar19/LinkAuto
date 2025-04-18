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

@Service
public interface ILinkAutoServiceProxy {
    
    // Authentication methods
    void register(User user);
    String login(Credentials credentials);
    void logout(String username);
    
    // User profile methods
    //LO HA HECHO MANOLO
    User getUserProfile(String username);
    void updateProfile(String username, User user);
    
    // Post methods
    void createPost(String token, PostCreator post);
    void deletePost(String token, Long postId);
    List<Post> getFeed(); //DEVUELVE TODOS LOS POSTS. CAMBIARLO
   
    //Obtener datos de un post en concreto. FALTA DE AÑADIR
    Post getPostById(int postId);

    User getUserByUsername(String username);

    List<User> getUserFollowers(String username);
    List<User> getUserFollowing(String username);
    void followUser(String token, String usernameToFollow);
    void unfollowUser(String token, String usernameToUnfollow);
    List<Post> getUserPosts(String username);
    List<Comment> getCommentsByPostId(long postId);
    void unlikePost(String token, Long postId);
    void likePost(String token, Long postId);
    void commentPost(String token, Long postId, CommentCreator comment);
}