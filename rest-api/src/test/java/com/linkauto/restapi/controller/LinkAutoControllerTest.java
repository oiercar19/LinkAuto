package com.linkauto.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.service.AuthService;
import com.linkauto.restapi.service.LinkAutoService;

public class LinkAutoControllerTest {

    LinkAutoService linkAutoService;
    AuthService authService;
    LinkAutoController linkAutoController;
    User usuario;

    @BeforeEach
    public void setUp() {
        linkAutoService = mock(LinkAutoService.class);
        authService = mock(AuthService.class);
        linkAutoController = new LinkAutoController(linkAutoService, authService);
        usuario = new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    @Test
    public void testGetAllPosts() {
        List<Post> posts = new ArrayList<>();

        Post post = new Post(1L, usuario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Comment comment = new Comment("testComment", usuario, post, 9999L);
        Comment comment2 = new Comment("testComment2", usuario, post, 9999L);
        post.addComentario(comment);
        post.addComentario(comment2);
        
        Post post2 = new Post(2L, usuario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        posts.add(post);
        posts.add(post2);
        when(linkAutoService.getAllPosts()).thenReturn(posts);
        
        ResponseEntity<List<PostReturnerDTO>> result = linkAutoController.getAllPosts();
        
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
    }

    @Test
    public void testGetPostById() {
        Post post = new Post(1L, usuario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        when(linkAutoService.getPostById(1L)).thenReturn(Optional.of(post));
        ResponseEntity<Post> result = linkAutoController.getPostById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());

        when(linkAutoService.getPostById(2L)).thenReturn(Optional.empty());
        ResponseEntity<Post> result2 = linkAutoController.getPostById(2L);
        assertEquals(HttpStatus.NOT_FOUND, result2.getStatusCode());
    }

    @Test
    public void createPost(){
        String userToken = "1234";

        when(authService.isTokenValid(userToken)).thenReturn(false);
        ResponseEntity<PostReturnerDTO> result = linkAutoController.createPost(userToken, new PostDTO("testPost", Arrays.asList("img1", "img2")));
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        when(authService.isTokenValid(userToken)).thenReturn(true);
        when(authService.getUserByToken(userToken)).thenReturn(usuario);
        Post post = new Post(1L, usuario, "testPost", 1234567, Arrays.asList("img1", "img2"), new ArrayList<>(), new HashSet<>());
        PostDTO postDTO = new PostDTO("testPost", Arrays.asList("img1", "img2"));
        when(linkAutoService.createPost(postDTO, usuario)).thenReturn(post);
        ResponseEntity<PostReturnerDTO> result2 = linkAutoController.createPost(userToken, postDTO);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertNotNull(result2.getBody());
        assertEquals("testPost", result2.getBody().getMessage());
        assertEquals(1L, result2.getBody().getId());
        assertEquals(1234567, result2.getBody().getCreationDate());
        assertEquals(Arrays.asList("img1", "img2"), result2.getBody().getImages());
    }

}
