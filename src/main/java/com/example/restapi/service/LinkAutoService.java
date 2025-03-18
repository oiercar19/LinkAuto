package com.example.restapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.example.restapi.model.Post;
import com.example.restapi.model.PostDTO;
import com.example.restapi.repository.PostRepository;

@Service
public class LinkAutoService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post createPost(PostDTO postDTO) {
        Post post = new Post();
        post.setUser(postDTO.getUser());
        post.setMessage(postDTO.getMessage());
        post.setUser(user); //Actual logged user
        post.setImages(postDTO.getImages());
        return postRepository.save(post);
    }

    public boolean deletePost(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
