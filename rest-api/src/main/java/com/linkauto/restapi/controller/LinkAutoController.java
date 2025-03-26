package com.linkauto.restapi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.service.AuthService;
import com.linkauto.restapi.service.LinkAutoService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "LinkAuto Controller", description = "API for managing social media")
public class LinkAutoController {

    @Autowired
    private final LinkAutoService linkAutoService;
    private final AuthService authService;

    public LinkAutoController(LinkAutoService linkAutoService, AuthService authService) {
        this.linkAutoService = linkAutoService;
        this.authService = authService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostReturnerDTO>> getAllPosts() {
        List<Post> posts = linkAutoService.getAllPosts();
        List<PostReturnerDTO> postReturnerDTOs = parsePostToPostReturnerDTO(posts);
        return ResponseEntity.ok(postReturnerDTOs);
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
        boolean isDeleted = linkAutoService.deletePost(id, user);
        return isDeleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = linkAutoService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String userToken, @RequestBody UserDTO userDetails) {
        try {
            if (!authService.isTokenValid(userToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            User userActual = authService.getUserByToken(userToken);
            User updatedUser = new User(userActual.getUsername(), userDetails.getName(), userDetails.getProfilePicture(), userToken!, null, 0, null, userToken, userToken, userToken)
            linkAutoService.updateUser(userToken, updatedUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private List<PostReturnerDTO> parsePostToPostReturnerDTO(List<Post> posts) {
        List<PostReturnerDTO> postReturnerDTOs = new ArrayList<>();
        for (Post post : posts) {
            PostReturnerDTO postReturnerDTO = new PostReturnerDTO(post.getId(), post.getUsuario().getUsername(), post.getMensaje(), post.getFechaCreacion(), post.getImagenes());
            postReturnerDTOs.add(postReturnerDTO);
        }
        return postReturnerDTOs;
    }
}
