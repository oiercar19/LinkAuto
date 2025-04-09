package com.linkauto.restapi.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.model.Post;
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
        
        PostDTO postDTO = new PostDTO("hola", new ArrayList<>());
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
    }



}
