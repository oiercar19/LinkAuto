package com.linkauto.restapi.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.CommentReturnerDTO;
import com.linkauto.restapi.dto.EventDTO;
import com.linkauto.restapi.dto.EventReturnerDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.dto.UpdateUserDTO;
import com.linkauto.restapi.dto.UserReturnerDTO;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.Event;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.Role;
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
    public ResponseEntity<PostReturnerDTO> getPostById(@PathVariable Long id) {
        Optional<Post> post = linkAutoService.getPostById(id);
        if (post.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PostReturnerDTO postReturnerDTO = parsePostToPostReturnerDTO(post.get());
        return ResponseEntity.ok(postReturnerDTO);
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
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserReturnerDTO userResult = parseUserToUserReturnerDTO(user);
        return ResponseEntity.ok(userResult);
    }

    @PutMapping("/user")
    public ResponseEntity<UpdateUserDTO> updateUser(
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken, 
        @RequestBody UpdateUserDTO userDetails) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        User requestingUser = authService.getUserByToken(userToken);
        User oldUser = authService.getUserByToken(userToken);
    
        // Verificar si el usuario que realiza la solicitud es administrador
        if (!requestingUser.getRole().equals(Role.ADMIN) && !requestingUser.getUsername().equals(oldUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    
        User updatedUser = parseUpdateUserDTOToUser(userDetails, oldUser);        
        return authService.updateUser(updatedUser, userToken) ? ResponseEntity.ok(parseUserToUpdateUserDTO(updatedUser)) : ResponseEntity.notFound().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserReturnerDTO>> getAllUsers() {
        // Obtener todos los usuarios
        List<User> users = linkAutoService.getAllUsers();

        // Convertir la lista de usuarios a UserReturnerDTO
        List<UserReturnerDTO> userReturnerDTOs = new ArrayList<>();
        for (User user : users) {
            userReturnerDTOs.add(parseUserToUserReturnerDTO(user));
        }

        for (UserReturnerDTO userReturnerDTO : userReturnerDTOs) {
            System.out.println(userReturnerDTO.isBanned());
        }
    
        return ResponseEntity.ok(userReturnerDTOs);
    }    

    @DeleteMapping("/user/{username}")
    public ResponseEntity<Void> deleteUser(
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken,
        @Parameter(name = "username", description = "Username of the user to delete", required = true, example = "johndoe")
        @PathVariable("username") String username
        ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User loggedUser = authService.getUserByToken(userToken);

        // Verificar que el usuario logueado es el mismo que se quiere borrar o que el usuario logueado es admin
        if (!loggedUser.getUsername().equals(username) && !loggedUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User targetUser = authService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isDeleted = authService.deleteUser(targetUser, userToken);
        return isDeleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/user/{username}/ban")
    public ResponseEntity<Void> banUser(
    @Parameter(name = "username", description = "Username of the user to ban or unban", required = true, example = "johndoe")
    @PathVariable String username,
    @Parameter(name = "banStatus", description = "Ban status (true to ban, false to unban)", required = true, example = "true")
    @RequestParam("banStatus") boolean banStatus,
    @Parameter(name = "userToken", description = "Token of the user making the request", required = true, example = "1234567890")
    @RequestParam("userToken") String userToken) {

        System.out.println("\n\n\n\nBan request received for username: " + username + ", banStatus: " + banStatus + "\\n" + //
                        "\n" + //
                        "\n" + //
                        "\n");   
    // Verificar si el token es válido
    if (!authService.isTokenValid(userToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Obtener el usuario que realiza la solicitud
    User requestingUser = authService.getUserByToken(userToken);

    // Verificar si el usuario que realiza la solicitud es administrador
    if (!requestingUser.getRole().equals(Role.ADMIN)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Obtener el usuario objetivo
    User targetUser = authService.getUserByUsername(username);
    if (targetUser == null) {
        return ResponseEntity.notFound().build();
    }

    // Actualizar el estado de baneo del usuario objetivo
    boolean isBanned = authService.banUser(username, banStatus);

    return isBanned ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
}

    @PutMapping("/user/{username}/role/admin")
    public ResponseEntity<Void> promoteToAdmin(
        @Parameter(name = "username", description = "Username of the user to promote", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(name = "userToken", description = "Token of the user making the request", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        // Verificar si el token es válido
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        // Obtener el usuario que realiza la solicitud
        User requestingUser = authService.getUserByToken(userToken);
    
        // Verificar si el usuario que realiza la solicitud es administrador
        if (!requestingUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    
        // Obtener el usuario objetivo
        User targetUser = authService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (requestingUser.equals(targetUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    
        // Cambiar el rol del usuario objetivo a ADMIN
        boolean isUpdated = authService.changeRole(targetUser, Role.ADMIN);
    
        return isUpdated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/user/{username}/role/user")
    public ResponseEntity<Void> demoteToUser(
        @Parameter(name = "username", description = "Username of the user to promote", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(name = "userToken", description = "Token of the user making the request", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        // Verificar si el token es válido
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        // Obtener el usuario que realiza la solicitud
        User requestingUser = authService.getUserByToken(userToken);
    
        // Verificar si el usuario que realiza la solicitud es administrador
        if (!requestingUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        
        // Obtener el usuario objetivo
        User targetUser = authService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (requestingUser.equals(targetUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    
        // Cambiar el rol del usuario objetivo a USER
        boolean isUpdated = authService.changeRole(targetUser, Role.USER);
    
        return isUpdated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
        Optional<User> user = linkAutoService.getUserByUsername(username);
        if (user.isPresent()) {
            return parseUserToUserReturnerDTO(user.get());
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
        boolean isFollowed = linkAutoService.followUser(user.getUsername(), username);
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
        @RequestBody CommentDTO comment
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isCommented = linkAutoService.commentPost(post_id, user, comment);
        return isCommented ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentReturnerDTO>> getAllComments() {
        List<Comment> comments = linkAutoService.getAllComments();
        List<CommentReturnerDTO> commentReturnerDTOs = new ArrayList<>();
        for (Comment comment : comments) {
            CommentReturnerDTO commentReturnerDTO = parseCommentToCommentReturnerDTO(comment);
            commentReturnerDTOs.add(commentReturnerDTO);
        }
        return ResponseEntity.ok(commentReturnerDTOs);
    }

    @GetMapping("/post/{post_id}/comments")
    public ResponseEntity<List<CommentReturnerDTO>> getCommentsByPostId(
        @Parameter(name = "post_id", description = "ID of the post", required = true, example = "1")
        @PathVariable Long post_id
    ) {
        List<Comment> comments = linkAutoService.getCommentsByPostId(post_id);
        List<CommentReturnerDTO> commentReturnerDTOs = new ArrayList<>();
        for (Comment comment : comments) {
            CommentReturnerDTO commentReturnerDTO = parseCommentToCommentReturnerDTO(comment);
            commentReturnerDTOs.add(commentReturnerDTO);
        }
        return ResponseEntity.ok(commentReturnerDTOs);
    }

    @PostMapping ("/user/{username}/report")
    public ResponseEntity<Void> reportUser(
        @Parameter(name = "username", description = "Username of the user to report", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(name = "userToken", description = "Token of the user making the report", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User reportingUser = authService.getUserByToken(userToken);
        User reportedUser = authService.getUserByUsername(username);

        if (reportedUser == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isReported = linkAutoService.reportUser(reportingUser, reportedUser);

        return isReported ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/user/{username}/verify")
    public ResponseEntity<Void> verifyUser(
        @Parameter(name = "username", description = "Username of the user to verify", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(name = "userToken", description = "Token of the user making the request", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
    if (!authService.isTokenValid(userToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }        
        User requestingUser = authService.getUserByToken(userToken);
        
        // Verificar si el usuario que realiza la solicitud es administrador
        // O si el usuario que realiza la solicitud es el mismo que se quiere verificar
        if (!requestingUser.getRole().equals(Role.ADMIN) && !requestingUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        // Obtener el usuario objetivo
        User targetUser = authService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        Boolean isVerified = linkAutoService.verifyUser(targetUser);
        if (isVerified == null) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        return isVerified ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    @GetMapping("/user/{username}/verify")
    public ResponseEntity<Boolean> isUserVerified(
        @Parameter(name = "username", description = "Username of the user to verify", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        User user = authService.getUserByUsername(username);
        System.out.println("User: " + user);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user.getIsVerified());
    }

    @PostMapping("/post/{post_id}/save")
    public ResponseEntity<Void> savePost(
        @Parameter(name = "post_id", description = "ID of the post to save", required = true, example = "1")
        @PathVariable Long post_id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isSaved = linkAutoService.savePost(post_id, user);
        return isSaved ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping ("/admin/{usernameOwningTheReports}/deleteReport/{usernameToDeleteReport}")
    public ResponseEntity<Void> deleteReport(
        @Parameter(name = "usernameToDeleteReport", description = "Username of the user to delete report", required = true, example = "johndoe")
        @PathVariable String usernameToDeleteReport,
        @Parameter(name = "usernameOwningTheReports", description = "Username of the user owning the reports", required = true, example = "marc21")
        @PathVariable String usernameOwningTheReports,
        @Parameter(name = "userToken", description = "Token of the user making the report", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verificar si es admin
        Boolean isAdmin = authService.getUserByToken(userToken).getRole().equals(Role.ADMIN);
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User userToDeleteReport = authService.getUserByUsername(usernameToDeleteReport);
        User userOwningTheReports = authService.getUserByUsername(usernameOwningTheReports);

        if (userOwningTheReports == null || userToDeleteReport == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isReported = linkAutoService.deleteReport(userToDeleteReport.getUsername(), userOwningTheReports.getUsername());

        return isReported ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

        @DeleteMapping("/post/{post_id}/unsave")
    public ResponseEntity<Void> unsavePost(
        @Parameter(name = "post_id", description = "ID of the post to unsave", required = true, example = "1")
        @PathVariable Long post_id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isUnSaved = linkAutoService.unsavePost(post_id, user);
        return isUnSaved ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/user/{username}/savedPosts")
    public ResponseEntity<List<PostReturnerDTO>> getSavedPostsByUsername(
        @Parameter(name = "username", description = "Username of the user", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        List<Post> savedPosts = linkAutoService.getSavedPostsByUsername(username);
        List<PostReturnerDTO> savedPostReturnerDTOs = parsePostsToPostReturnerDTO(savedPosts);
        return ResponseEntity.ok(savedPostReturnerDTOs);
    }

    public List<PostReturnerDTO> parsePostsToPostReturnerDTO(List<Post> posts) {

        List<PostReturnerDTO> postReturnerDTOs = new ArrayList<>();
        for (Post post : posts) {
            List<Long> comment_ids = new ArrayList<>();
            for (Comment comment : post.getComentarios()) {
                comment_ids.add(comment.getId());
            }

            PostReturnerDTO postReturnerDTO = new PostReturnerDTO(post.getId(), post.getUsuario().getUsername(), post.getMensaje(), post.getFechaCreacion(), post.getImagenes(), comment_ids, post.getLikes());
            postReturnerDTOs.add(postReturnerDTO);
        }
        return postReturnerDTOs;
    }

    private PostReturnerDTO parsePostToPostReturnerDTO(Post post) {
        List<Long> comment_ids = new ArrayList<>();
        for (Comment comment : post.getComentarios()) {
            comment_ids.add(comment.getId());
        }
    
        PostReturnerDTO postReturnerDTO = new PostReturnerDTO(post.getId(), post.getUsuario().getUsername(), post.getMensaje(), post.getFechaCreacion(), post.getImagenes(), comment_ids, post.getLikes());
        return postReturnerDTO;
    }

    private User parseUpdateUserDTOToUser(UpdateUserDTO userDTO, User oldUser) {
        User u = new User(oldUser.getUsername(), userDTO.getName(), userDTO.getProfilePicture(), userDTO.getEmail(), userDTO.getCars(), userDTO.getBirthDate(), User.Gender.valueOf(userDTO.getGender().toUpperCase()), userDTO.getLocation(), userDTO.getPassword(), userDTO.getDescription(),  oldUser.getPosts(), oldUser.getFollowers(), oldUser.getFollowing(), oldUser.getSavedPosts());
        u.setRole(oldUser.getRole());
        u.setIsVerified(oldUser.getIsVerified());
        for (User reporter : oldUser.getReporters()) {
            u.getReporters().add(reporter);
            
        };
        u.setBanned(oldUser.isBanned());
        return u;
    }

    private UserReturnerDTO parseUserToUserReturnerDTO(User u){
        List<PostReturnerDTO> postReturner = parsePostsToPostReturnerDTO(u.getPosts());
        Set<UserReturnerDTO> reporters = new HashSet<>();
        for (User user: u.getReporters()) {
            reporters.add(parseUserToUserReturnerDTO(user));
        }
        
        List<Post> savedPosts = new ArrayList<>(u.getSavedPosts());
        List<PostReturnerDTO> savedPost = parsePostsToPostReturnerDTO(savedPosts);
        return new UserReturnerDTO(u.getUsername(), u.getRole().toString() , u.isBanned() , u.getName(), u.getProfilePicture(), u.getEmail(), u.getCars(), u.getBirthDate(), u.getGender().toString(), u.getLocation(), u.getPassword(), u.getDescription(), postReturner, savedPost, u.getIsVerified(), reporters);
    }

    private UpdateUserDTO parseUserToUpdateUserDTO(User u){
        return new UpdateUserDTO(u.getUsername(), u.getName(), u.getProfilePicture(), u.getEmail(), u.getCars(), u.getBirthDate(), u.getGender().toString(), u.getLocation(), u.getPassword(), u.getDescription());
    }

    private CommentReturnerDTO parseCommentToCommentReturnerDTO(Comment comment) {
        return new CommentReturnerDTO(comment.getId(), comment.getText(), comment.getUser().getUsername(), comment.getPost().getId(), comment.getCreationDate());
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventReturnerDTO>> getAllEvents() {
        List<Event> events = linkAutoService.getAllEvents();
        List<EventReturnerDTO> eventReturnerDTOs = parseEventsToEventReturnerDTO(events);
        return ResponseEntity.ok(eventReturnerDTOs);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventReturnerDTO> getEventById(@PathVariable Long id) {
        Optional<Event> event = linkAutoService.getEventById(id);
        if (event.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        EventReturnerDTO eventReturnerDTO = parseEventToEventReturnerDTO(event.get());
        return ResponseEntity.ok(eventReturnerDTO);
    }

    @PostMapping("/events")
    public ResponseEntity<EventReturnerDTO> createEvent(
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken,
        @Parameter(name = "eventDTO", description = "Event data", required = true)
        @RequestBody EventDTO eventDTO
        ) {

        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        Event createdEvent = linkAutoService.createEvent(eventDTO, user);
        EventReturnerDTO eventReturnerDTO = parseEventToEventReturnerDTO(createdEvent);
        return ResponseEntity.ok(eventReturnerDTO);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(
        @Parameter(name = "id", description = "Event ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
        ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isDeleted = linkAutoService.deleteEvent(id, user);
        return isDeleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/events/{id}/participate")
    public ResponseEntity<Void> participateInEvent(
        @Parameter(name = "id", description = "Event ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
        ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isParticipating = linkAutoService.participateInEvent(id, user);
        return isParticipating ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/events/{id}/cancel")
    public ResponseEntity<Void> cancelParticipation(
        @Parameter(name = "id", description = "Event ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken
        ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getUserByToken(userToken);
        boolean isCancelled = linkAutoService.cancelParticipation(id, user);
        return isCancelled ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/events/{id}/participants")
    public ResponseEntity<Set<String>> getEventParticipants(
        @Parameter(name = "id", description = "Event ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        // Get the set of usernames of event participants
        Set<String> participantUsernames = linkAutoService.getEventParticipants(id);
        
        // If there are no participants or the event doesn't exist
        if (participantUsernames == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(participantUsernames);
    }

    @GetMapping("/user/{username}/events")
    public ResponseEntity<List<EventReturnerDTO>> getUserEvents(
        @Parameter(name = "username", description = "Username of the user", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        List<Event> events = linkAutoService.getEventsByUsername(username);
        List<EventReturnerDTO> eventReturnerDTOs = parseEventsToEventReturnerDTO(events);
        return ResponseEntity.ok(eventReturnerDTOs);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<EventReturnerDTO> updateEvent(
        @Parameter(name = "id", description = "Event ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
        @RequestParam("userToken") String userToken,
        @Parameter(name = "eventDTO", description = "Updated event data", required = true)
        @RequestBody EventDTO eventDTO
    ) {
        if (!authService.isTokenValid(userToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = authService.getUserByToken(userToken);
        Optional<Event> updatedEventOpt = linkAutoService.updateEvent(id, eventDTO, user);
        
        if (updatedEventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        EventReturnerDTO eventReturnerDTO = parseEventToEventReturnerDTO(updatedEventOpt.get());
        return ResponseEntity.ok(eventReturnerDTO);
    }

    // Helper methods for converting Event objects to DTOs
    private List<EventReturnerDTO> parseEventsToEventReturnerDTO(List<Event> events) {
        List<EventReturnerDTO> eventReturnerDTOs = new ArrayList<>();
        for (Event event : events) {
            EventReturnerDTO eventReturnerDTO = parseEventToEventReturnerDTO(event);
            eventReturnerDTOs.add(eventReturnerDTO);
        }
        return eventReturnerDTOs;
    }

    private EventReturnerDTO parseEventToEventReturnerDTO(Event event) {
        // Get comments IDs if the event has comments
        List<Long> commentIds = new ArrayList<>();
        // If the event has comments, add them
        // Note: You might need to modify this part based on your Event model structure
        if (event.getComentarios() != null) {
            for (Comment comment : event.getComentarios()) {
                commentIds.add(comment.getId());
            }
        }
        
        return new EventReturnerDTO(
            event.getId(),
            event.getCreador().getUsername(),
            event.getTitulo(),
            event.getDescripcion(),
            event.getUbicacion(),
            event.getFechaInicio(),
            event.getFechaFin(),
            event.getImagenes(),
            new HashSet<>(event.getParticipantes()),
            commentIds
        );
    }
}