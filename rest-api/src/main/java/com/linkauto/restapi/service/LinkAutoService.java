package com.linkauto.restapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.PostRepository;
import com.linkauto.restapi.repository.UserRepository;

@Service
public class LinkAutoService {
    @Autowired
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LinkAutoService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post createPost(PostDTO postDTO, User user) {
        Post post = new Post();
        post.setMensaje(postDTO.getMessage());
        post.setUsuario(user); //Actual logged user
        for (String imagen : postDTO.getImages()) {
            post.addImagen(imagen); //image url
        }
        post.setFechaCreacion(System.currentTimeMillis());
        postRepository.save(post);
        user.addPost(post); // Add post to user
        userRepository.save(user); // Save user with the new post
        return post;
    }
    
    @Transactional
    public boolean deletePost(Long id, User user) {
        // Buscar el post
        Post post = postRepository.findById(id)
            .orElse(null);
        
        // Verificar si el post existe
        if (post == null) {
            return false;
        }
        
        // Verificar si el usuario tiene permiso para borrar el post
        if (!post.getUsuario().getUsername().equals(user.getUsername())) {
            return false;
        }
        
        try {            
            // Desasociar el post del usuario
            User postUser = post.getUsuario();
            if (postUser != null) {
                postUser.getPosts().remove(post);
            }
            
            // Limpiar imágenes
            post.getImagenes().clear();
            
            // Eliminar post
            postRepository.delete(post);
            postRepository.flush();
            
            return true;
        } catch (Exception e) {
            // Loguear el error
            System.err.println("Error al eliminar post: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<Post> getPostsByUsername(String username) {
        return postRepository.findByUsuario_Username(username);
    }
}

