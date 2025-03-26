package com.example.restapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.UserRepository;

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

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(String username, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(userDetails.getName());
            user.setProfilePicture(userDetails.getProfilePicture());
            user.setEmail(userDetails.getEmail());
            user.setCars(userDetails.getCars());
            user.setBirthDate(userDetails.getBirthDate());
            user.setGender(userDetails.getGender());
            user.setLocation(userDetails.getLocation());
            user.setPassword(userDetails.getPassword());
            user.setDescription(userDetails.getDescription());
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void deleteUser(String username) {
        if (userRepository.existsById(username)) {
            userRepository.deleteById(username);
        } else {
            throw new RuntimeException("User not found with username: " + username);
        }
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

