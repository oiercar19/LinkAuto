package com.example.restapi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.PostDTO;
import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;
import com.example.restapi.service.LinkAutoService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "LinkAuto Controller", description = "API for managing social media")
public class LinkAutoController {

    @Autowired
    private LinkAutoService linkAutoService;
    private AuthService authService;

    public LinkAutoController(LinkAutoService linkAutoService, AuthService authService) {
        this.linkAutoService = linkAutoService;
        this.authService = authService;
    }

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
    public ResponseEntity<Post> createPost(
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken,
        @Parameter(name = "postDTO", description = "Post data", required = true)
        @RequestBody PostDTO postDTO
        ) {

        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        Post createdPost = linkAutoService.createPost(postDTO, user);
        return ResponseEntity.ok(createdPost);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(
        @Parameter(name = "id", description = "Post ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
        ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        if (!user.equals(linkAutoService.getPostById(id).get().getUsuario())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isDeleted = linkAutoService.deletePost(id);
        return isDeleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = linkAutoService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*@PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String username, @RequestBody User userDetails) {
        try {
            User updatedUser = linkAutoService.updateUser(username, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }*/
}
