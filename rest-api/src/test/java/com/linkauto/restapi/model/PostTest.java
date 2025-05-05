package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.linkauto.restapi.model.User.Gender;

public class PostTest {

    private Post post;

    @BeforeEach
    public void setUp() {
        post = new Post(1L, new User(), "test message", 999999999L, Arrays.asList("image1.jpg", "image2.jpg"), new ArrayList<>(), new HashSet<>());
    }

    @Test
    public void testGettersAndSetters() {
        User user = new User();
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");

        assertEquals(1L, post.getId());
        assertEquals(user, post.getUsuario());
        assertEquals("test message", post.getMensaje());
        assertEquals(999999999L, post.getFechaCreacion());
        assertEquals(images, post.getImagenes());
        assertTrue(post.getComentarios().isEmpty());
        assertTrue(post.getLikes().isEmpty());

        post.setMensaje("updated message");
        assertEquals("updated message", post.getMensaje());

        post.setFechaCreacion(123456789L);
        assertEquals(123456789L, post.getFechaCreacion());
    }

    @Test
    public void testSetComentarios() {
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();
        List<Comment> comentarios = Arrays.asList(comment1, comment2);

        post.setComentarios(comentarios);

        assertEquals(2, post.getComentarios().size());
        assertTrue(post.getComentarios().contains(comment1));
        assertTrue(post.getComentarios().contains(comment2));
    }

    @Test
    public void testAddComentario() {
        Comment comment = new Comment();
        post.addComentario(comment);

        assertEquals(1, post.getComentarios().size());
        assertTrue(post.getComentarios().contains(comment));
    }

    @Test
    public void testSetAndRemoveLikes() {
        post.setLikes("user1");
        post.setLikes("user2");

        assertEquals(2, post.getLikes().size());
        assertTrue(post.getLikes().contains("user1"));
        assertTrue(post.getLikes().contains("user2"));

        post.removeLikes("user1");
        assertEquals(1, post.getLikes().size());
        assertFalse(post.getLikes().contains("user1"));
        assertTrue(post.getLikes().contains("user2"));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqualsAndHashCode() {
        User user1 = new User(
            "u1",
            "Name",
            "pic",
            "e@mail",
            Arrays.asList("car1"),
            12345L,
            User.Gender.OTHER,
            "Loc",
            "pwd",
            "Desc",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        List<String> images = List.of("img.jpg");
        List<Comment> comments = new ArrayList<>();
        HashSet<String> likes = new HashSet<>();
        
        //Post iguales
        Post post1 = new Post(1L, user1, "Mensaje A", 123L, images, comments, likes);
        Post post2 = new Post(1L, user1, "Mensaje A", 123L, images, comments, likes);
        user1.addPost(post1);
        user1.addPost(post2);
    
        assertEquals(post1, post2);  // Mismo ID, deberían ser iguales
        assertEquals(post1.hashCode(), post2.hashCode());  // El hashCode debe ser el mismo
    
        // Distinto ID
        Post post3 = new Post(2L, user1, "Mensaje A", 123L, images, new ArrayList<>(), new HashSet<>());
    
        assertNotEquals(post1, post3);  // Distinto ID
        assertNotEquals(post2, post3);  // Distinto ID
    
        // ID null
        Post post4 = null;
        Post post5 = null;
    
        assertEquals(post4, post5);  // Ambos tienen ID null, deberían ser iguales
        
        // Distinto tipo de objeto
        Post post6 = new Post(1L, user1, "Mensaje A", 123L, images, comments, likes);
        String notAPost = "Not a Post Object";

        assertNotEquals(post6, notAPost);  // Debería ser false porque no es del mismo tipo

        Post post7 = new Post(1L, user1, "Mensaje A", 123L, images, comments, likes);
        assertFalse(post7.equals(null));  // Check equals(null)
        
        // Diferente usuario
        User user3 = new User("testUsername", "testName", "testProfilePicture", "testEmail", new ArrayList<>(), 123456L, Gender.MALE, "testLocation", "testPassword", "testDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Post post8 = new Post(1L, user3, "Mensaje A", 123L, images, comments, likes);
        assertNotEquals(post7, post8);  // Distinto usuario
        assertNotEquals(post7.hashCode(), post8.hashCode());  // Distinto usuario, hashCode diferente

        // Diferentes imagenes
        List<String> images2 = List.of("img2.jpg");
        Post post9 = new Post(1L, user3, "Mensaje A", 123L, images2, comments, likes);
        assertNotEquals(post8, post9);  // Distintas imagenes
        assertNotEquals(post8.hashCode(), post9.hashCode());  // Distintas imagenes, hashCode diferente
        
        // Diferentes comentarios
        List<Comment> comments2 = new ArrayList<>();
        
        Post post10 = new Post(1L, user3, "Mensaje A", 123L, images, comments2, likes);
        Post post11 = new Post(1L, user3, "Mensaje A", 123L, images, new ArrayList<>(), likes);
        Comment c1 = new Comment("test", user3, post11, 123L);
        post11.addComentario(c1);
        
        assertNotEquals(post10, post11);  // Distintos comentarios
        assertNotEquals(post10.hashCode(), post11.hashCode());  // Distintos comentarios, hashCode diferente

        // Diferentes likes
        HashSet<String> likes2 = new HashSet<>();
        likes2.add("user1");
        Post post12 = new Post(1L, user3, "Mensaje A", 123L, images, comments, likes2);
        Post post13 = new Post(1L, user3, "Mensaje A", 123L, images, comments, new HashSet<>());
        assertNotEquals(post12, post13);  // Distintos likes
        assertNotEquals(post12.hashCode(), post13.hashCode());  // Distintos likes, hashCode diferente    
    
        // Diferente mensaje
        Post post14 = new Post(1L, user3, "Mensaje B", 123L, images, comments, likes);
        assertNotEquals(post12, post14);  // Distinto mensaje
        assertNotEquals(post12.hashCode(), post14.hashCode());  // Distinto mensaje, hashCode diferente
    }
    

    @Test
    public void testToString() {
        User user = new User("testUsername", "testName", "testProfilePicture", "testEmail", new ArrayList<>(), 123456L, Gender.MALE, "testLocation", "testPassword", "testDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        Post post1 = new Post(1L, user, "test message", 999999999L, images, new ArrayList<>(), new HashSet<>());
        System.out.println(post1.toString());
        String expected = "Post [id=1, usuario=" + user.getUsername() + ", mensaje=test message, fechaCreacion=999999999, imagenes=[image1.jpg, image2.jpg], comentarios=[], likes=[]]";
        assertEquals(expected, post1.toString());
    }

    @Test
    public void constructorTest() {
        User user3 = new User("testUsername", "testName", "testProfilePicture", "testEmail", new ArrayList<>(), 123456L, Gender.MALE, "testLocation", "testPassword", "testDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        List<Comment> comments = new ArrayList<>();
        Comment comment1 = new Comment("test", user3, post, 123L);
        comments.add(comment1);
        Comment comment2 = new Comment("test2", user3, post, 123L);
        comments.add(comment2);
        
        HashSet<String> likes = new HashSet<>();
        likes.add("user1");
        likes.add("user2");
        Post postInstance = new Post(1L, user3, "test message", 999999999L, images, comments, likes);
        assertNotNull(postInstance);
    }
    
}
