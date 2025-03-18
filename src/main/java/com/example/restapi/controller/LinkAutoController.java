package com.example.restapi.controller;

import com.example.restapi.model.Book;
import com.example.restapi.service.BookService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "LinkAuto Controller", description = "API for managing social media")
public class LinkAutoController {

    @Autowired
    private LinkAutoService linkAutoService;

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = linkAutoService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = linkAutoService.getPostById(id);
        return post.isPresent() ? ResponseEntity.ok(post.get()) : ResponseEntity.notFound().build();
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody PostDTO postDTO) {
        Post createdPost = linkAutoService.createPost(postDTO);
        return ResponseEntity.ok(createdPost);
    }
}
