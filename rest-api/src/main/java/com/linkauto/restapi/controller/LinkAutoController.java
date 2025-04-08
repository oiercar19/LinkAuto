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
import com.linkauto.restapi.dto.UserDTO;
import com.linkauto.restapi.dto.UserReturnerDTO;
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
        List<PostReturnerDTO> postReturnerDTOs = parsePostsToPostReturnerDTO(posts);
        return ResponseEntity.ok(postReturnerDTOs);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = linkAutoService.getPostById(id);
        return post.isPresent() ? ResponseEntity.ok(post.get()) : ResponseEntity.notFound().build();
    }

    @PostMapping("/posts")
    public ResponseEntity<PostReturnerDTO> createPost(
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
        PostReturnerDTO postReturnerDTO = parsePostToPostReturnerDTO(createdPost);
        return ResponseEntity.ok(postReturnerDTO);
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

    @GetMapping("/user")
    public ResponseEntity<UserReturnerDTO> getUserDetails(
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken) {
        User user = authService.getUserByToken(userToken);
        UserReturnerDTO userResult = parseUserToUserReturnerDTO(user);
        return userResult != null ? ResponseEntity.ok(userResult) : ResponseEntity.notFound().build();
    }

    @PutMapping("/user")
    public ResponseEntity<User> updateUser(
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken, 
        @RequestBody UserDTO userDetails) {
            if (!authService.isTokenValid(userToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            User oldUser = authService.getUserByToken(userToken);
            User updatedUser = parseUserDTOToUser(userDetails, oldUser);
            return authService.updateUser(updatedUser, userToken) ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{username}/posts")
    public ResponseEntity<List<PostReturnerDTO>> getUserPosts(
        @Parameter(name = "username", description = "Username of the user", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        List<Post> posts = linkAutoService.getPostsByUsername(username);
        List<PostReturnerDTO> postReturnerDTOs = parsePostsToPostReturnerDTO(posts);
        return ResponseEntity.ok(postReturnerDTOs);
    }

    @GetMapping("/user/{username}")
    public UserReturnerDTO getUserByUsername(
        @Parameter(name = "username", description = "Username of the user", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        User user = linkAutoService.getUserByUsername(username).get();
        if (user != null) {
            return parseUserToUserReturnerDTO(user);
        }
        return null;
    }

    @GetMapping("/user/{username}/followers")
    public ResponseEntity<List<UserReturnerDTO>> getUserFollowers(
        @Parameter(name = "username", description = "Username of the user", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        List<User> followers = linkAutoService.getFollowersByUsername(username);
        List<UserReturnerDTO> followersDTOs = new ArrayList<>();
        for (User follower : followers) {
            followersDTOs.add(parseUserToUserReturnerDTO(follower));
        }
        return ResponseEntity.ok(followersDTOs);
    }

    @GetMapping("/user/{username}/following")
    public ResponseEntity<List<UserReturnerDTO>> getUserFollowing(
        @Parameter(name = "username", description = "Username of the user", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        List<User> following = linkAutoService.getFollowingByUsername(username);
        List<UserReturnerDTO> followingDTOs = new ArrayList<>();
        for (User follow : following) {
            followingDTOs.add(parseUserToUserReturnerDTO(follow));
        }
        return ResponseEntity.ok(followingDTOs);
    }

    @PostMapping("/user/{username}/follow")
    public ResponseEntity<Void> followUser(
        @Parameter(name = "username", description = "Username of the user to follow", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isFollowed = linkAutoService.followUser(user, username);
        return isFollowed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/user/{username}/unfollow")
    public ResponseEntity<Void> unfollowUser(
        @Parameter(name = "username", description = "Username of the user to unfollow", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isUnfollowed = linkAutoService.unfollowUser(user, username);
        return isUnfollowed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        
    }
    
    @PostMapping("/user/{post_id}/like")
    public ResponseEntity<Void> likePost(
        @Parameter(name = "post_id", description = "ID of the post to like", required = true, example = "1")
        @PathVariable Long post_id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isLiked = linkAutoService.likePost(post_id, user.getUsername());
        return isLiked ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/user/{post_id}/unlike")
    public ResponseEntity<Void> unlikePost(
        @Parameter(name = "post_id", description = "ID of the post to unlike", required = true, example = "1")
        @PathVariable Long post_id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isUnliked = linkAutoService.unlikePost(post_id, user.getUsername());
        return isUnliked ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/user/{post_id}/comment")
    public ResponseEntity<Void> commentPost(
        @Parameter(name = "post_id", description = "ID of the post to comment", required = true, example = "1")
        @PathVariable Long post_id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken,
        @RequestBody String comment
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isCommented = linkAutoService.commentPost(post_id, user, comment);
        return isCommented ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    private List<PostReturnerDTO> parsePostsToPostReturnerDTO(List<Post> posts) {
        List<PostReturnerDTO> postReturnerDTOs = new ArrayList<>();
        for (Post post : posts) {
            PostReturnerDTO postReturnerDTO = new PostReturnerDTO(post.getId(), post.getUsuario().getUsername(), post.getMensaje(), post.getFechaCreacion(), post.getImagenes());
            postReturnerDTOs.add(postReturnerDTO);
        }
        return postReturnerDTOs;
    }

    private PostReturnerDTO parsePostToPostReturnerDTO(Post post) {
        PostReturnerDTO postReturnerDTO = new PostReturnerDTO(post.getId(), post.getUsuario().getUsername(), post.getMensaje(), post.getFechaCreacion(), post.getImagenes());
        return postReturnerDTO;
    }

    private User parseUserDTOToUser(UserDTO userDTO, User oldUser) {
        return new User(oldUser.getUsername(), userDTO.getName(), userDTO.getProfilePicture(), userDTO.getEmail(), userDTO.getCars(), userDTO.getBirthDate(), User.Gender.valueOf(userDTO.getGender().toUpperCase()), userDTO.getLocation(), userDTO.getPassword(), userDTO.getDescription(),  oldUser.getPosts(), oldUser.getFollowers(), oldUser.getFollowing());
    }

    private UserReturnerDTO parseUserToUserReturnerDTO(User u){
        List<PostReturnerDTO> postReturner = parsePostsToPostReturnerDTO(u.getPosts());
        return new UserReturnerDTO(u.getUsername(), u.getName(), u.getProfilePicture(), u.getEmail(), u.getCars(), u.getBirthDate(), u.getGender().toString(), u.getLocation(), u.getPassword(), u.getDescription(), postReturner);
    }

    
}
