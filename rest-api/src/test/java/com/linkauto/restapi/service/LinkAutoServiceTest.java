package com.linkauto.restapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
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
import com.linkauto.restapi.repository.EventRepository;
import com.linkauto.restapi.repository.PostRepository;
import com.linkauto.restapi.repository.UserRepository;


public class LinkAutoServiceTest {
    
    private PostRepository postRepository;
    private UserRepository userRepository;
    private CommentRepository commentRepository;
    private EventRepository eventRepository;
    private LinkAutoService linkAutoService;

    @BeforeEach
    public void setUp() {
        postRepository = mock(PostRepository.class);
        userRepository = mock(UserRepository.class);
        commentRepository = mock(CommentRepository.class);
        eventRepository = mock(EventRepository.class);
        linkAutoService = new LinkAutoService(postRepository, userRepository, commentRepository, eventRepository);
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
        User user = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()
        );

        Post expectedPost = new Post();
        expectedPost.setMensaje(postDTO.getMessage());
        expectedPost.setUsuario(user);
        for (String imagen : postDTO.getImages()) {
            expectedPost.addImagen(imagen);
        }

        // Simula el comportamiento de save devolviendo el post con fecha
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setFechaCreacion(System.currentTimeMillis()); // o una fecha fija si quieres
            return savedPost;
        });
        when(userRepository.save(user)).thenReturn(user);

        Post actualPost = linkAutoService.createPost(postDTO, user);

        // Validaciones campo por campo
        assertEquals(expectedPost.getMensaje(), actualPost.getMensaje());
        assertEquals(expectedPost.getUsuario(), actualPost.getUsuario());
        assertEquals(expectedPost.getImagenes(), actualPost.getImagenes());
        assertNotNull(actualPost.getFechaCreacion());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void testDeletePost() {
        User usuarioPropietario = new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User usuarioExterno = new User("externalUsername", "externalName", "externalProfilePicture", "externalEmail", new ArrayList<>(), 654321L, Gender.FEMALE, "externalLocation", "externalPassword", "externalDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
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
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), followers, new ArrayList<>(), new HashSet<>())));
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
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), followings, new HashSet<>())));
        List<User> result = linkAutoService.getFollowingByUsername("testUsername");
        assertEquals(followings, result);
        assertEquals(2, result.size());

        when(userRepository.findByUsername("nonExistentUsername")).thenReturn(Optional.empty());
        List<User> result2 = linkAutoService.getFollowingByUsername("nonExistentUsername");
        assertEquals(null, result2);
    }

    @Test
    public void testFollowUser(){
        User user = new User("user", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User otherUser = new User("otherUser", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.save(otherUser)).thenReturn(otherUser);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        Boolean result2 = linkAutoService.followUser(user.getUsername(), otherUser.getUsername());
        assertTrue(result2);

        verify(userRepository).save(otherUser);
        verify(userRepository).save(user);
    }

    @Test
    public void testUnfollowUser(){
        User userToUnfollow = new User("user1" , "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User user = new User("user2", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), Arrays.asList(userToUnfollow), new ArrayList<>(), new HashSet<>());

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
        User user = new User("user1", "User One", "", "", new ArrayList<>(), 123456L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
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
        List<User> users = List.of(new User("user1", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>())
        , new User("user2", "externalName", "externalProfilePicture", "externalEmail", new ArrayList<>(), 654321L, Gender.FEMALE, "externalLocation", "externalPassword", "externalDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()));

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
        User user = new User("testUser", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
    
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
    public void testReportUserSuccess() {
        User reporter = new User(); reporter.setUsername("reporter");
        User reported = new User(); reported.setUsername("reported");

        when(userRepository.findByUsername("reporter")).thenReturn(Optional.of(reporter));
        when(userRepository.findByUsername("reported")).thenReturn(Optional.of(reported));

        boolean result = linkAutoService.reportUser(reporter, reported);
        assertTrue(result);
        assertTrue(reported.getReporters().contains(reporter));
        verify(userRepository).save(reported);
    }

    @Test
    public void testReportUserReporterNotFound() {
        User reporter = new User(); reporter.setUsername("reporter");
        User reported = new User(); reported.setUsername("reported");

        when(userRepository.findByUsername("reporter")).thenReturn(Optional.empty());

        boolean result = linkAutoService.reportUser(reporter, reported);
        assertFalse(result);
    }

    @Test
    public void testReportUserReportedNotFound() {
        User reporter = new User(); reporter.setUsername("reporter");
        User reported = new User(); reported.setUsername("reported");

        when(userRepository.findByUsername("reporter")).thenReturn(Optional.of(reporter));
        when(userRepository.findByUsername("reported")).thenReturn(Optional.empty());

        boolean result = linkAutoService.reportUser(reporter, reported);
        assertFalse(result);
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
    public void testGetAllEvents() {
        List<com.linkauto.restapi.model.Event> events = new ArrayList<>();
        com.linkauto.restapi.model.Event event1 = new com.linkauto.restapi.model.Event();
        com.linkauto.restapi.model.Event event2 = new com.linkauto.restapi.model.Event();
        events.add(event1);
        events.add(event2);

        when(eventRepository.findAll()).thenReturn(events);

        List<com.linkauto.restapi.model.Event> result = linkAutoService.getAllEvents();

        assertEquals(2, result.size());
        assertEquals(event1, result.get(0));
        assertEquals(event2, result.get(1));
        verify(eventRepository).findAll();
    }

    @Test
    public void testGetEventById_Found() {
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Optional<com.linkauto.restapi.model.Event> result = linkAutoService.getEventById(1L);

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
        verify(eventRepository).findById(1L);
    }

    @Test
    public void testGetEventById_NotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<com.linkauto.restapi.model.Event> result = linkAutoService.getEventById(2L);

        assertFalse(result.isPresent());
        verify(eventRepository).findById(2L);
    }

    @Test
    public void testCreateEvent_WithImages() {
        com.linkauto.restapi.dto.EventDTO eventDTO = new com.linkauto.restapi.dto.EventDTO(
            "Title", "Description", "Location", 123L, 456L, Arrays.asList("img1", "img2")
        );
        User user = new User("creator", "Creator Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>() );

        com.linkauto.restapi.model.Event result = linkAutoService.createEvent(eventDTO, user);

        assertEquals("Title", result.getTitulo());
        assertEquals("Description", result.getDescripcion());
        assertEquals("Location", result.getUbicacion());
        assertEquals(123L, result.getFechaInicio());
        assertEquals(456L, result.getFechaFin());
        assertEquals(user, result.getCreador());
        assertEquals(Arrays.asList("img1", "img2"), result.getImagenes());
        verify(eventRepository).save(result);
    }

    @Test
    public void testCreateEvent_WithoutImages() {
        com.linkauto.restapi.dto.EventDTO eventDTO = new com.linkauto.restapi.dto.EventDTO(
            "Title2", "Description2", "Location2", 789L, 1011L, null
        );
        User user = new User("creator2", "Creator2", "", "", new ArrayList<>(), 0L, Gender.FEMALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(userRepository.save(user)).thenReturn(user);

        com.linkauto.restapi.model.Event result = linkAutoService.createEvent(eventDTO, user);

        assertEquals("Title2", result.getTitulo());
        assertEquals("Description2", result.getDescripcion());
        assertEquals("Location2", result.getUbicacion());
        assertEquals(789L, result.getFechaInicio());
        assertEquals(1011L, result.getFechaFin());
        assertEquals(user, result.getCreador());
        assertTrue(result.getImagenes().isEmpty());
        verify(eventRepository).save(result);
    }

    @Test
    public void testDeleteEvent_Success() {
        User creator = new User("creator", "Creator", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.setCreador(creator);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        boolean result = linkAutoService.deleteEvent(1L, creator);

        assertTrue(result);
        verify(eventRepository).delete(event);
        verify(eventRepository).flush();
    }

    @Test
    public void testDeleteEvent_EventNotFound() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = linkAutoService.deleteEvent(2L, user);
        assertFalse(result);
    }


    @Test
    public void testDeleteReport_Success() {
        User userReported = new User();
        userReported.setUsername("reported");
        User userReporter = new User();
        userReporter.setUsername("reporter");
        userReported.getReporters().add(userReporter);

        when(userRepository.findByUsername("reported")).thenReturn(Optional.of(userReported));
        when(userRepository.findByUsername("reporter")).thenReturn(Optional.of(userReporter));
        when(userRepository.save(userReported)).thenReturn(userReported);

        boolean result = linkAutoService.deleteReport(userReported, "reporter");
        assertTrue(result);
        assertFalse(userReported.getReporters().contains(userReporter));
        verify(userRepository).save(userReported);
    }

    @Test
    public void testDeleteReport_UserReportedNotFound() {
        User userReported = new User();
        userReported.setUsername("reported");

        when(userRepository.findByUsername("reported")).thenReturn(Optional.empty());

        boolean result = linkAutoService.deleteReport(userReported, "reporter");
        assertFalse(result);
    }

    @Test
    public void testDeleteEvent_UserNotCreator() {
        User creator = new User("creator", "Creator", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User otherUser = new User("other", "Other", "", "", new ArrayList<>(), 0L, Gender.FEMALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.setCreador(creator);

        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));

        boolean result = linkAutoService.deleteEvent(3L, otherUser);

        assertFalse(result);
    }

    @Test
    public void testDeleteEvent_CreadorNull() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.setCreador(null);

        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));

        boolean result = linkAutoService.deleteEvent(4L, user);

        assertFalse(result);
    }

    @Test
    public void testDeleteEvent_Exception() {
        User creator = new User("creator", "Creator", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.setCreador(creator);

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        doNothing().when(eventRepository).delete(event);
        doNothing().when(eventRepository).flush();
        // Simulate exception on delete
        doNothing().when(eventRepository).delete(event);
        doNothing().when(eventRepository).flush();
        // Actually, to simulate exception, we need to throw:
        org.mockito.Mockito.doThrow(new RuntimeException("DB error")).when(eventRepository).delete(event);

        boolean result = linkAutoService.deleteEvent(5L, creator);

        assertFalse(result);
    }

    @Test
    public void testParticipateInEvent_Success() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        boolean result = linkAutoService.participateInEvent(1L, user);

        assertTrue(result);
        assertTrue(event.getParticipantes().contains("user"));
        verify(eventRepository).save(event);
    }

    @Test
    public void testParticipateInEvent_EventNotFound() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = linkAutoService.participateInEvent(2L, user);

        assertFalse(result);
    }

    @Test
    public void testCancelParticipation_Success() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.addParticipante("user");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        boolean result = linkAutoService.cancelParticipation(1L, user);

        assertTrue(result);
        assertFalse(event.getParticipantes().contains("user"));
        verify(eventRepository).save(event);
    }

    @Test
    public void testCancelParticipation_EventNotFound() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());

        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = linkAutoService.cancelParticipation(2L, user);

        assertFalse(result);
    }

    @Test
    public void testGetEventParticipants_Found() {
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.addParticipante("user1");
        event.addParticipante("user2");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Set<String> result = linkAutoService.getEventParticipants(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("user2"));
    }

    @Test
    public void testGetEventParticipants_EventNotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        Set<String> result = linkAutoService.getEventParticipants(2L);

        assertEquals(null, result);
    }

    @Test
    public void testGetEventsByCreador_Found() {
        User creator = new User("creator", "Creator", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        List<com.linkauto.restapi.model.Event> events = Arrays.asList(new com.linkauto.restapi.model.Event(), new com.linkauto.restapi.model.Event());

        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(eventRepository.findByCreador_Username("creator")).thenReturn(events);

        List<com.linkauto.restapi.model.Event> result = linkAutoService.getEventsByCreador("creator");

        assertEquals(events, result);
    }

    @Test
    public void testGetEventsByCreador_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        List<com.linkauto.restapi.model.Event> result = linkAutoService.getEventsByCreador("unknown");

        assertTrue(result == null || result.isEmpty());
    }

    @Test
    public void testCommentEvent_Success() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        CommentDTO commentDTO = new CommentDTO("Test comment");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.save(org.mockito.Mockito.any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(event)).thenReturn(event);

        boolean result = linkAutoService.commentEvent(1L, user, commentDTO);

        assertTrue(result);
        assertEquals(1, event.getComentarios().size());
        assertEquals("Test comment", event.getComentarios().get(0).getText());
        verify(commentRepository).save(org.mockito.Mockito.any(Comment.class));
        verify(eventRepository).save(event);
    }

    @Test
    public void testCommentEvent_EventNotFound() {
        User user = new User("user", "User", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        CommentDTO commentDTO = new CommentDTO("Test comment");

        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = linkAutoService.commentEvent(2L, user, commentDTO);

        assertFalse(result);
    }

    @Test
    public void testGetCommentsByEventId_Found() {
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        Comment c1 = new Comment();
        c1.setText("Comment 1");
        Comment c2 = new Comment();
        c2.setText("Comment 2");
        event.setComentarios(Arrays.asList(c1, c2));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        List<Comment> result = linkAutoService.getCommentsByEventId(1L);

        assertEquals(2, result.size());
        assertEquals("Comment 1", result.get(0).getText());
        assertEquals("Comment 2", result.get(1).getText());
    }

    @Test
    public void testGetCommentsByEventId_EventNotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        List<Comment> result = linkAutoService.getCommentsByEventId(2L);

        assertEquals(null, result);
    }

    @Test
    public void testGetUserParticipatingEvents_Found() {
        String username = "user";
        List<com.linkauto.restapi.model.Event> events = Arrays.asList(new com.linkauto.restapi.model.Event(), new com.linkauto.restapi.model.Event());

        when(eventRepository.findByParticipantesContaining(username)).thenReturn(events);

        List<com.linkauto.restapi.model.Event> result = linkAutoService.getUserParticipatingEvents(username);

        assertEquals(events, result);
    }

    @Test
    public void testGetUserParticipatingEvents_UsernameNullOrEmpty() {
        List<com.linkauto.restapi.model.Event> result1 = linkAutoService.getUserParticipatingEvents(null);
        List<com.linkauto.restapi.model.Event> result2 = linkAutoService.getUserParticipatingEvents("");

        assertEquals(null, result1);
        assertEquals(null, result2);
    }

    @Test
    public void testGetEventsByUsername_CombinesCreatedAndParticipating() {
        String username = "user";
        com.linkauto.restapi.model.Event createdEvent = new com.linkauto.restapi.model.Event();
        com.linkauto.restapi.model.Event participatingEvent = new com.linkauto.restapi.model.Event();

        List<com.linkauto.restapi.model.Event> createdEvents = new ArrayList<>();
        createdEvents.add(createdEvent);

        List<com.linkauto.restapi.model.Event> participatingEvents = new ArrayList<>();
        participatingEvents.add(participatingEvent);

        when(eventRepository.findByCreador_Username(username)).thenReturn(createdEvents);
        when(eventRepository.findByParticipantesContaining(username)).thenReturn(participatingEvents);

        List<com.linkauto.restapi.model.Event> result = linkAutoService.getEventsByUsername(username);

        assertEquals(1, result.size());
        assertTrue(result.contains(createdEvent));
        assertTrue(result.contains(participatingEvent));
    }

    @Test
    public void testGetEventsByUsername_NoDuplicates() {
        String username = "user";
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();

        List<com.linkauto.restapi.model.Event> createdEvents = new ArrayList<>();
        createdEvents.add(event);

        List<com.linkauto.restapi.model.Event> participatingEvents = new ArrayList<>();
        participatingEvents.add(event);

        when(eventRepository.findByCreador_Username(username)).thenReturn(createdEvents);
        when(eventRepository.findByParticipantesContaining(username)).thenReturn(participatingEvents);

        List<com.linkauto.restapi.model.Event> result = linkAutoService.getEventsByUsername(username);

        assertEquals(1, result.size());
        assertTrue(result.contains(event));
    }

    @Test
    public void testUpdateEvent_Success() {
        User creator = new User("creator", "Creator",  "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.setCreador(creator);
        event.setImagenes(new ArrayList<>(Arrays.asList("oldImg")));
        com.linkauto.restapi.dto.EventDTO eventDTO = new com.linkauto.restapi.dto.EventDTO(
            "NewTitle", "NewDesc", "NewLoc", 100L, 200L, Arrays.asList("img1", "img2")
        );

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        Optional<com.linkauto.restapi.model.Event> result = linkAutoService.updateEvent(1L, eventDTO, creator);

        assertTrue(result.isPresent());
        assertEquals("NewTitle", result.get().getTitulo());
        assertEquals("NewDesc", result.get().getDescripcion());
        assertEquals("NewLoc", result.get().getUbicacion());
        assertEquals(100L, result.get().getFechaInicio());
        assertEquals(200L, result.get().getFechaFin());
        assertEquals(Arrays.asList("img1", "img2"), result.get().getImagenes());
        verify(eventRepository).save(event);
    }

    @Test
    public void testUpdateEvent_EventNotFound() {
        User creator = new User("creator", "Creator",  "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.dto.EventDTO eventDTO = new com.linkauto.restapi.dto.EventDTO(
            "Title", "Desc", "Loc", 100L, 200L, null
        );

        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<com.linkauto.restapi.model.Event> result = linkAutoService.updateEvent(2L, eventDTO, creator);

        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateEvent_UserNotCreator() {
        User creator = new User("creator", "Creator",  "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User otherUser = new User("other", "Other",  "", "", new ArrayList<>(), 0L, Gender.FEMALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        com.linkauto.restapi.model.Event event = new com.linkauto.restapi.model.Event();
        event.setCreador(creator);
        com.linkauto.restapi.dto.EventDTO eventDTO = new com.linkauto.restapi.dto.EventDTO(
            "Title", "Desc", "Loc", 100L, 200L, null
        );

        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));

        Optional<com.linkauto.restapi.model.Event> result = linkAutoService.updateEvent(3L, eventDTO, otherUser);

        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteReport_UserReporterNotFound() {
        User userReported = new User();
        userReported.setUsername("reported");

        when(userRepository.findByUsername("reported")).thenReturn(Optional.of(userReported));
        when(userRepository.findByUsername("reporter")).thenReturn(Optional.empty());

        boolean result = linkAutoService.deleteReport(userReported, "reporter");
        assertTrue(result); // removeReporters(null) is called, but method still returns true
        verify(userRepository).save(userReported);
    }

    @Test
    public void testDeleteReport_UsernameIsNull() {
        User userReported = new User();
        userReported.setUsername("reported");

        when(userRepository.findByUsername("reported")).thenReturn(Optional.of(userReported));

        boolean result = linkAutoService.deleteReport(userReported, null);
        assertFalse(result);
    }

    public void testVerifyUser() {
        // Arrange
        User user = new User("testUser", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
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
        User user2 = new User("testUser2", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        user2.addPost(new Post());
        user2.addPost(new Post());
        user2.addFollower(new User());
        user2.addFollower(new User());
        user2.addFollower(new User());
        Boolean result2 = linkAutoService.verifyUser(user2);
        assertFalse(result2);

        // Case with less than 3 followers
        User user3 = new User("testUser3", "Test Name", "", "", new ArrayList<>(), 0L, Gender.MALE, "", "password", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        user3.addPost(new Post());
        user3.addPost(new Post());
        user3.addPost(new Post());
        user3.addFollower(new User());
        user3.addFollower(new User());
        Boolean result3 = linkAutoService.verifyUser(user3);
        assertFalse(result3);
    }

    public void testGetSavedPostsByUsername() {
        // Preparar posts guardados para el usuario
        Post post1 = new Post(1L, new User(), "Post 1", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Post post2 = new Post(2L, new User(), "Post 2", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        HashSet<Post> savedPosts = new HashSet<>(Arrays.asList(post1, post2));
        
        // Crear usuario con posts guardados
        User user = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), savedPosts
        );
        
        // Caso exitoso: usuario existe
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(user));
        List<Post> result = linkAutoService.getSavedPostsByUsername("testUsername");
        
        // Verificar que el resultado no es nulo y contiene los posts guardados
        assertNotNull(result, "El resultado no debería ser nulo");
        assertEquals(2, result.size(), "Deberían haber 2 posts guardados");
        // Verificamos que contiene los posts específicos (puede variar el orden al usar HashSet)
        assertTrue(result.stream().anyMatch(p -> p.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(p -> p.getId().equals(2L)));
        
        // Caso fallido: usuario no existe
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        List<Post> resultNull = linkAutoService.getSavedPostsByUsername("nonExistentUser");
        
        assertNull(resultNull, "El resultado debería ser nulo cuando el usuario no existe");
    }
    
    @Test
    public void testSavePost() {
        // Crear post a guardar
        Post postToSave = new Post(1L, new User(), "Test post", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        // Crear usuario que guardará el post
        User userFromRequest = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()
        );
        
        // Usuario completo desde la base de datos (con Set<Post> sincronizado)
        User userFromDB = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()
        );
        
        // Caso exitoso: post y usuario existen
        when(postRepository.findById(1L)).thenReturn(Optional.of(postToSave));
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(userFromDB));
        when(userRepository.save(userFromDB)).thenReturn(userFromDB);
        
        boolean result = linkAutoService.savePost(1L, userFromRequest);
        
        assertTrue(result);
        assertTrue(userFromDB.getSavedPosts().contains(postToSave));
        verify(userRepository).save(userFromDB);
        
        // Caso fallido: post no existe
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        
        boolean resultPostNotFound = linkAutoService.savePost(2L, userFromRequest);
        
        assertFalse(resultPostNotFound);
        
        // Caso fallido: usuario nulo
        // Primero restauramos el mock para findById para evitar conflictos
        when(postRepository.findById(3L)).thenReturn(Optional.of(new Post()));
        
        // Este caso simula que el usuario en el servicio es nulo
        boolean resultUserNotNull = linkAutoService.savePost(3L, userFromDB);
        assertTrue(resultUserNotNull);
        
        // Caso fallido: usuario no existe en la base de datos
        when(userRepository.findByUsername("validUser")).thenReturn(Optional.empty());
        
        //boolean resultUserNotFound = linkAutoService.savePost(3L, validUser);
        //assertFalse(resultUserNotFound);
        
        // Caso fallido: post ya está guardado
        Post existingPost = new Post(4L, new User(), "Existing post", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        HashSet<Post> postsSet = new HashSet<>();
        postsSet.add(existingPost);
        User userWithExistingPost = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), postsSet
        );
        
        when(postRepository.findById(4L)).thenReturn(Optional.of(existingPost));
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(userWithExistingPost));
        
        boolean resultAlreadySaved = linkAutoService.savePost(4L, userFromRequest);
        
        assertFalse(resultAlreadySaved);
    }
    
    @Test
    public void testUnsavePost() {
        // Crear post a eliminar de guardados
        Post postToUnsave = new Post(1L, new User(), "Test post", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        
        // Crear usuario que tiene el post guardado
        User userFromRequest = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()
        );
        
        // Usuario completo desde la base de datos con el post guardado
        HashSet<Post> savedPosts = new HashSet<>();
        savedPosts.add(postToUnsave);
        User userFromDB = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), savedPosts
        );
        
        // Caso exitoso: post existe y está guardado
        when(postRepository.findById(1L)).thenReturn(Optional.of(postToUnsave));
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(userFromDB));
        when(userRepository.save(userFromDB)).thenReturn(userFromDB);
        doNothing().when(userRepository).flush();
        
        boolean result = linkAutoService.unsavePost(1L, userFromRequest);
        
        assertTrue(result);
        assertFalse(userFromDB.getSavedPosts().contains(postToUnsave));
        verify(userRepository).save(userFromDB);
        verify(userRepository).flush();
        
        // Caso fallido: post no existe
        when(postRepository.findById(2L)).thenReturn(Optional.empty());
        
        boolean resultPostNotFound = linkAutoService.unsavePost(2L, userFromRequest);
        
        assertFalse(resultPostNotFound);
        
        
        boolean resultUserNull = linkAutoService.unsavePost(1L, userFromDB);
        assertFalse(resultUserNull);
        
        // Caso fallido: usuario no existe en la base de datos
        when(userRepository.findByUsername("validUser")).thenReturn(Optional.empty());
        
        //boolean resultUserNotFound = linkAutoService.unsavePost(1L, validUser);
        //assertFalse(resultUserNotFound);
        
        // Caso fallido: post no está en los guardados del usuario
        Post differentPost = new Post(3L, new User(), "Different post", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        User userWithoutPost = new User(
            "testUsername", "testName", "testProfilePicture", "testEmail",
            new ArrayList<>(), 123456L, Gender.MALE, "testLocation",
            "testPassword", "testDescription",
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()
        );
        
        when(postRepository.findById(3L)).thenReturn(Optional.of(differentPost));
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(userWithoutPost));
        
        boolean resultNotSaved = linkAutoService.unsavePost(3L, userFromRequest);
        
        assertFalse(resultNotSaved);
    }
}
