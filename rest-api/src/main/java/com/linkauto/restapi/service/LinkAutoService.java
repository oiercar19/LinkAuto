package com.linkauto.restapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private final AuthService authService;

    public LinkAutoService(PostRepository postRepository, UserRepository userRepository, AuthService authService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authService = authService;
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
        return post;
    }

    public boolean deletePost(Long id, User user) {
        if (!postRepository.existsById(id)) {
            return false;
        }
        if (!postRepository.findById(id).get().getUsuario().getUsername().equals(user.getUsername())) {
            return false;
        }
        postRepository.deleteById(id);
        return true;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User userDetails) {
        return userRepository.save(userDetails);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

