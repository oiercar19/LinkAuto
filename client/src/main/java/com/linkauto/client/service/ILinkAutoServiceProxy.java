/**
 * Interface for the LinkAuto service proxy.
 * Defines the contract for communication with the backend service.
 */
package com.linkauto.client.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public User getUserByUsername(String username);
}