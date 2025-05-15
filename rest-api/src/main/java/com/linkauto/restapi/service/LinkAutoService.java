package com.linkauto.restapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.CommentRepository;
import com.linkauto.restapi.repository.PostRepository;
import com.linkauto.restapi.repository.UserRepository;

@Service
public class LinkAutoService {
    @Autowired
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public LinkAutoService(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
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
        try {            
            // Buscar el post
            Post post = postRepository.findById(id)
                .orElse(null);
            
            // Verificar si el post existe
            if (post == null) {
                return false;
            }

            // Verificar si el usuario tiene permiso para borrar el post
            if (post.getUsuario() == null || !post.getUsuario().getUsername().equals(user.getUsername())) {
                return false;
            }
            
            // Desasociar el post del usuario
            User postUser = post.getUsuario();
            postUser.getPosts().remove(post);
            userRepository.save(postUser);
            
            // Limpiar imágenes
            post.getImagenes().clear();
            postRepository.save(post);
            
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

    public List<User> getFollowersByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }
        return user.getFollowers();
    }

    public List<User> getFollowingByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }
        return user.getFollowing();
    }

    public Boolean followUser(User user, String usernameToFollow) {
        User userToFollow = userRepository.findByUsername(usernameToFollow).orElse(null);
        if (userToFollow == null) {
            return false;
        }
        user.addFollowing(userToFollow);
        userToFollow.addFollower(user);
        userRepository.save(user);
    
        return true;
    }

    public Boolean unfollowUser(User user, String usernameToUnfollow) {
        User userToUnfollow = userRepository.findByUsername(usernameToUnfollow).orElse(null);
        if (userToUnfollow == null) {
            return false;
        }
        user.removeFollowing(userToUnfollow);
        userToUnfollow.removeFollower(user);
        userRepository.save(user);
        userRepository.save(userToUnfollow);
        userRepository.flush();
    
        return true;
    }

    public Boolean likePost (Long postId, String username) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return false;
        }
        post.setLikes(username);
        postRepository.save(post);
    
        return true;
    }

    public Boolean unlikePost (Long postId, String username) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return false;
        }
        post.removeLikes(username);
        postRepository.save(post);
    
        return true;
    }

    public Boolean commentPost (Long postId, User u, CommentDTO comment) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return false;
        }
        Post p = postRepository.findById(postId).orElse(null);
        Comment c = new Comment();
        c.setUser(u);
        c.setText(comment.getText());
        c.setPost(p);
        c.setCreationDate(System.currentTimeMillis());
        post.addComentario(c);
        postRepository.save(post);
    
        return true;
    }

    public Boolean reportUser (User user, User userReport) {
        User userReporter = userRepository.findByUsername(user.getUsername()).orElse(null);
        User userToReport = userRepository.findByUsername(userReport.getUsername()).orElse(null);
        
        if (userReporter == null || userToReport == null) {
            return false;
        }
        
        userToReport.setReporters(userReporter);
        userRepository.save(userToReport);

        return true;
    }

    public Boolean deleteReport (User user, String username) {
        User userReported = userRepository.findByUsername(user.getUsername()).orElse(null);
        User userReporter = userRepository.findByUsername(username).orElse(null);

        if (userReported == null || username == null) {
            return false;
        }
        
        userReported.removeReporters(userReporter);
        userRepository.save(userReported);

        return true;
    }


    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return null;
        }
        return post.getComentarios();
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

