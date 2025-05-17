package com.linkauto.restapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.repository.CommentRepository;
import com.linkauto.restapi.repository.PostRepository;
import com.linkauto.restapi.repository.UserRepository;


public class LinkAutoServiceTest {
    
    private PostRepository postRepository;
    private UserRepository userRepository;
    private CommentRepository commentRepository;
    private LinkAutoService linkAutoService;

    @BeforeEach
    public void setUp() {
        postRepository = mock(PostRepository.class);
        userRepository = mock(UserRepository.class);
        commentRepository = mock(CommentRepository.class);
        linkAutoService = new LinkAutoService(postRepository, userRepository, commentRepository);
    }

    @Test
    public void testGetAllPosts() {

        //Test with empty list
        List<Post> expectedPosts = new ArrayList<>();
        when(postRepository.findAll()).thenReturn(expectedPosts);
        
        List<Post> actualPosts = linkAutoService.getAllPosts();
        assertEquals(expectedPosts, actualPosts);

        //Test with non-empty list
        List<Post> expectedPosts2 = new ArrayList<>();
        when(postRepository.findAll()).thenReturn(expectedPosts2);

        List<Post> actualPosts2 = linkAutoService.getAllPosts();
        assertEquals(expectedPosts2, actualPosts2);

    }

    @Test
    public void testGetPostById() {
        //Test with empty post
        Post expectedPost = new Post();
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(expectedPost));
        
        Post actualPost = linkAutoService.getPostById(1L).orElse(null);
        assertEquals(expectedPost, actualPost);

        //Test with non-empty post
        Post expectedPost2 = new Post(2L, new User(), "hola", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(postRepository.findById(2L)).thenReturn(java.util.Optional.of(expectedPost2));

        Post actualPost2 = linkAutoService.getPostById(2L).orElse(null);
        assertEquals(expectedPost2, actualPost2);
    }

    @Test
    public void testCreatePost() {
        
        PostDTO postDTO = new PostDTO("hola", Arrays.asList("image1", "image2"));
        User user = new User("testUsername", "testName", "testProfilePicture", "testEmail", new ArrayList<>(), 123456L, Gender.MALE, "testLocation", "testPassword", "testDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Post expectedPost = new Post();
        expectedPost.setMensaje(postDTO.getMessage());
        expectedPost.setUsuario(user);
        for (String imagen : postDTO.getImages()) {
            expectedPost.addImagen(imagen); //image url
        }
        when(postRepository.save(expectedPost)).thenReturn(expectedPost);
        when(userRepository.save(user)).thenReturn(user);
        
        Post actualPost = linkAutoService.createPost(postDTO, user);
        expectedPost.setFechaCreacion(actualPost.getFechaCreacion());
        assertEquals(expectedPost, actualPost);
        verify(postRepository).save(actualPost);
    }

    @Test
    public void testDeletePost() {
        User usuarioPropietario = new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        User usuarioExterno = new User("externalUsername", "externalName", "externalProfilePicture", "externalEmail", new ArrayList<>(), 654321L, Gender.FEMALE, "externalLocation", "externalPassword", "externalDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        //Test with empty post
        Post post = new Post(1L, usuarioPropietario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(post));
        
        boolean result = linkAutoService.deletePost(1L, usuarioExterno);
        assertEquals(false, result);

        result = linkAutoService.deletePost(1L, usuarioPropietario);
        assertEquals(true, result);

        verify(postRepository).delete(post);
    
        //Test con post null
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        Boolean result2 = linkAutoService.deletePost(2L, usuarioPropietario);
        assertEquals(false, result2);

        //Test con user de post null
        Post post2 = new Post(3L, null, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(postRepository.findById(3L)).thenReturn(java.util.Optional.of(post2));
        Boolean result3 = linkAutoService.deletePost(3L, usuarioPropietario);
        assertEquals(false, result3);

        when(postRepository.findById(100L)).thenThrow(new IllegalArgumentException());
        assertFalse(linkAutoService.deletePost(100L, usuarioPropietario));
    }

    @Test
    public void testGetFollowersByUsername() {
        List<User> followers = new ArrayList<>();
        followers.add(new User());
        followers.add(new User());
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), followers, new ArrayList<>())));
        List<User> result = linkAutoService.getFollowersByUsername("testUsername");
        assertEquals(followers, result);
        assertEquals(2, result.size());

        when(userRepository.findByUsername("nonExistentUsername")).thenReturn(Optional.empty());
        List<User> result2 = linkAutoService.getFollowersByUsername("nonExistentUsername");
        assertEquals(null, result2);
    } 
    
    @Test
    public void testGetFollowingByUsername() {
        List<User> followings = new ArrayList<>();
        followings.add(new User());
        followings.add(new User());
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), followings)));
        List<User> result = linkAutoService.getFollowingByUsername("testUsername");
        assertEquals(followings, result);
        assertEquals(2, result.size());

        when(userRepository.findByUsername("nonExistentUsername")).thenReturn(Optional.empty());
        List<User> result2 = linkAutoService.getFollowingByUsername("nonExistentUsername");
        assertEquals(null, result2);
    }

    @Test
    public void testFollowUser(){
        User userToFollow = new User("user1", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        User user = new User("user2", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        when(userRepository.save(user)).thenReturn(user);


        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(userToFollow));
        Boolean result1 = linkAutoService.followUser(user, "user1");
        assertTrue(result1);

        when(userRepository.findByUsername("nullUser")).thenReturn(Optional.empty());
        Boolean result2 = linkAutoService.followUser(user, "nullUser");
        assertFalse(result2);

        verify(userRepository).save(user);
    }

    @Test
    public void testUnfollowUser(){
        User userToUnfollow = new User("user1" , "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        User user = new User("user2", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), Arrays.asList(userToUnfollow), new ArrayList<>());

        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.save(userToUnfollow)).thenReturn(userToUnfollow);
        doNothing().when(userRepository).flush();
        


        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(userToUnfollow));
        Boolean result1 = linkAutoService.unfollowUser(user, "user1");
        assertTrue(result1);

        when(userRepository.findByUsername("nullUser")).thenReturn(Optional.empty());
        Boolean result2 = linkAutoService.unfollowUser(user, "nullUser");
        assertFalse(result2);

        verify(userRepository).save(user);
        verify(userRepository).save(userToUnfollow);
    }

    @Test
    public void testCommentPost() {
        User user = new User("user1", "User One", "", "", new ArrayList<>(), 123456L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Post post = new Post(1L, user, "Post message", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        CommentDTO commentDTO = new CommentDTO("This is a comment");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        
        boolean result = linkAutoService.commentPost(1L, user, commentDTO);
        assertTrue(result);
        
        // Verifying the post now has the comment
        assertEquals(1, post.getComentarios().size());
        assertEquals("This is a comment", post.getComentarios().get(0).getText());
        verify(postRepository).save(post);

        // Case with null post
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        boolean result2 = linkAutoService.commentPost(2L, user, commentDTO);
        assertFalse(result2);
    }

    @Test
    public void testLikePost() {
        Post post = new Post(1L, new User(), "Post message", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        boolean result = linkAutoService.likePost(1L, "user1");
        assertTrue(result);
        assertTrue(post.getLikes().contains("user1"));
        verify(postRepository).save(post);

        // Case with null post
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        boolean result2 = linkAutoService.likePost(2L, "user1");
        assertFalse(result2);
    }

    @Test
    public void testUnlikePost() {
        Post post = new Post(1L, new User(), "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        post.setLikes("user1");
    
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
    
        Boolean result = linkAutoService.unlikePost(1L, "user1");
        assertTrue(result);  
    
        assertFalse(post.getLikes().contains("user1"));
    
        verify(postRepository).save(post);

        // Case with null post
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        boolean result2 = linkAutoService.unlikePost(2L, "user1");
        assertFalse(result2);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = List.of(new User("user1", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
        , new User("user2", "externalName", "externalProfilePicture", "externalEmail", new ArrayList<>(), 654321L, Gender.FEMALE, "externalLocation", "externalPassword", "externalDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = linkAutoService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    public void testGetAllComments() {
        List<Comment> comments = List.of(
            new Comment(),
            new Comment()
        );
        comments.get(0).setText("Comentario 1");
        comments.get(1).setText("Comentario 2");

        when(commentRepository.findAll()).thenReturn(comments);

        List<Comment> result = linkAutoService.getAllComments();

        assertEquals(2, result.size());
        assertEquals("Comentario 1", result.get(0).getText());
        verify(commentRepository).findAll();
    }

    @Test
    public void testGetUserByUsername_Found() {
        User user = new User("testUser", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
    
        Optional<User> result = linkAutoService.getUserByUsername("testUser");
    
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userRepository).findByUsername("testUser");
    }
    
    @Test
    public void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());
    
        Optional<User> result = linkAutoService.getUserByUsername("nonExistingUser");
    
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonExistingUser");
    }


    @Test
    public void testGetPostsByUsername() {
        // Arrange
        String username = "usuario1";
        Post post1 = new Post();
        Post post2 = new Post();
    
        when(postRepository.findByUsuario_Username(username)).thenReturn(List.of(post1, post2));
    
        // Act
        List<Post> result = linkAutoService.getPostsByUsername(username);
    
        // Assert
        assertEquals(2, result.size());
        verify(postRepository).findByUsuario_Username(username);
    }

    @Test
    public void testGetCommentsByPostId() {
        // Arrange
        Long postId = 1L;
        Comment comment1 = new Comment();
        comment1.setText("Comment 1");
        Comment comment2 = new Comment();
        comment2.setText("Comment 2");
        Post post = new Post();
        post.setComentarios(List.of(comment1, comment2));

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // Act
        List<Comment> result = linkAutoService.getCommentsByPostId(postId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Comment 1", result.get(0).getText());
        assertEquals("Comment 2", result.get(1).getText());
        verify(postRepository).findById(postId);

        // Case with null post
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        List<Comment> result2 = linkAutoService.getCommentsByPostId(2L);
        assertEquals(null, result2);
    }

    @Test
    public void testVerifyUser() {
        // Arrange
        User user = new User("testUser", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        user.addPost(new Post());
        user.addPost(new Post());
        user.addPost(new Post());
        user.addFollower(new User());
        user.addFollower(new User());
        user.addFollower(new User());
        
        when(userRepository.save(user)).thenReturn(user);

        Boolean result = linkAutoService.verifyUser(user);

        // Assert
        assertTrue(result);
        assertTrue(user.getIsVerified());
        verify(userRepository).save(user);

        // Case with less than 3 posts
        User user2 = new User("testUser2", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        user2.addPost(new Post());
        user2.addPost(new Post());
        user2.addFollower(new User());
        user2.addFollower(new User());
        user2.addFollower(new User());
        Boolean result2 = linkAutoService.verifyUser(user2);
        assertFalse(result2);

        // Case with less than 3 followers
        User user3 = new User("testUser3", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        user3.addPost(new Post());
        user3.addPost(new Post());
        user3.addPost(new Post());
        user3.addFollower(new User());
        user3.addFollower(new User());
        Boolean result3 = linkAutoService.verifyUser(user3);
        assertFalse(result3);
    }

}
