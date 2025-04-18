package com.linkauto.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

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

    @BeforeEach
    public void setUp() {
        linkAutoService = mock(LinkAutoService.class);
        authService = mock(AuthService.class);
        linkAutoController = new LinkAutoController(linkAutoService, authService);
    }
    
    @Test
    public void testGetAllPosts() {
        User usuarioPropietario = new User("ownerUsername", "ownerName", "ownerProfilePicture", "ownerEmail", new ArrayList<>(), 123456L, Gender.MALE, "ownerLocation", "ownerPassword", "ownerDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<Post> posts = new ArrayList<>();

        Post post = new Post(1L, usuarioPropietario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Comment comment = new Comment("testComment", usuarioPropietario, post, 9999L);
        Comment comment2 = new Comment("testComment2", usuarioPropietario, post, 9999L);
        post.addComentario(comment);
        post.addComentario(comment2);
        
        Post post2 = new Post(2L, usuarioPropietario, "testMessage", 1234567, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        posts.add(post);
        posts.add(post2);
        when(linkAutoService.getAllPosts()).thenReturn(posts);
        
        ResponseEntity<List<PostReturnerDTO>> result = linkAutoController.getAllPosts();
        
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
    }



}
