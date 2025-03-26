/**
 * Interface for the LinkAuto service proxy.
 * Defines the contract for communication with the backend service.
 */
package com.linkauto.client.service;

import java.util.List;

import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.CredencialesDTO;
import com.linkauto.restapi.model.Post;

public interface ILinkAutoServiceProxy {
    
    // Authentication methods
    void register(User user);
    User login(CredencialesDTO credentials);
    void logout(String username);
    
    // User profile methods
    //LO HA HECHO MANOLO
    User getUserProfile(String username);
    void updateProfile(String username, User user);
    
    // Post methods
    void createPost(String username, Post post);
    void deletePost(String username, int postId);
    List<Post> getFeed(); //DEVUELVE TODOS LOS POSTS. CAMBIARLO
   
    //Obtener datos de un post en concreto. FALTA DE AÑADIR
    Post getPostById(int postId);
}