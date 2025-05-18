package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.linkauto.restapi.model.User.Gender;

public class EventTest {

    private Event event;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("testUsername", "testName", "testProfilePicture", "testEmail", 
                new ArrayList<>(), 123456L, Gender.MALE, "testLocation", "testPassword", 
                "testDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        event = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
    }

    @Test
    public void testGettersAndSetters() {
        // Verificar los getters iniciales
        assertEquals(1L, event.getId());
        assertEquals(user, event.getCreador());
        assertEquals("Test Event", event.getTitulo());
        assertEquals("Event Description", event.getDescripcion());
        assertEquals("Test Location", event.getUbicacion());
        assertEquals(1000000000L, event.getFechaInicio());
        assertEquals(1000001000L, event.getFechaFin());
        assertEquals(1000000000L, event.getEventDate());
        assertEquals(2, event.getImagenes().size());
        assertTrue(event.getParticipantes().isEmpty());
        assertTrue(event.getComentarios().isEmpty());

        // Probar los setters
        User newUser = new User("newUser", "New Name", "newPic", "new@email.com", 
                new ArrayList<>(), 987654L, Gender.FEMALE, "New Location", "newPassword", 
                "New Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        event.setCreador(newUser);
        assertEquals(newUser, event.getCreador());

        event.setTitulo("Updated Event");
        assertEquals("Updated Event", event.getTitulo());

        event.setDescripcion("Updated Description");
        assertEquals("Updated Description", event.getDescripcion());

        event.setUbicacion("Updated Location");
        assertEquals("Updated Location", event.getUbicacion());

        event.setFechaInicio(2000000000L);
        assertEquals(2000000000L, event.getFechaInicio());
        assertEquals(2000000000L, event.getEventDate());

        event.setFechaFin(2000001000L);
        assertEquals(2000001000L, event.getFechaFin());
        
        // Probar el setter específico de eventDate
        event.setEventDate(3000000000L);
        assertEquals(3000000000L, event.getEventDate());
    }

    @Test
    public void testImagenes() {
        assertEquals(2, event.getImagenes().size());
        assertTrue(event.getImagenes().contains("image1.jpg"));
        assertTrue(event.getImagenes().contains("image2.jpg"));
        
        event.addImagen("image3.jpg");
        assertEquals(3, event.getImagenes().size());
        assertTrue(event.getImagenes().contains("image3.jpg"));
    }

    @Test
    public void testParticipantes() {
        assertTrue(event.getParticipantes().isEmpty());
        
        event.addParticipante("user1");
        assertEquals(1, event.getParticipantes().size());
        assertTrue(event.getParticipantes().contains("user1"));
        
        event.addParticipante("user2");
        assertEquals(2, event.getParticipantes().size());
        assertTrue(event.getParticipantes().contains("user2"));
        
        event.removeParticipante("user1");
        assertEquals(1, event.getParticipantes().size());
        assertFalse(event.getParticipantes().contains("user1"));
        assertTrue(event.getParticipantes().contains("user2"));
    }

    @Test
    public void testComentarios() {
        assertTrue(event.getComentarios().isEmpty());
        
        Comment comment1 = new Comment("test comment 1", user, null, 123456L);
        event.addComentario(comment1);
        assertEquals(1, event.getComentarios().size());
        assertTrue(event.getComentarios().contains(comment1));
        
        Comment comment2 = new Comment("test comment 2", user, null, 123457L);
        List<Comment> comentarios = Arrays.asList(comment1, comment2);
        event.setComentarios(comentarios);
        
        assertEquals(2, event.getComentarios().size());
        assertTrue(event.getComentarios().contains(comment1));
        assertTrue(event.getComentarios().contains(comment2));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqualsAndHashCode() {
        // Eventos iguales
        Event event1 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        Event event2 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        
        // Eventos con distinto ID
        Event event3 = new Event(2L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event3);
        assertNotEquals(event1.hashCode(), event3.hashCode());
        
        // Distinto tipo de objeto
        String notAnEvent = "Not an Event Object";
        assertNotEquals(event1, notAnEvent);
        
        // Check equals(null)
        assertFalse(event1.equals(null));
        
        // Diferentes creadores
        User user2 = new User("user2", "Name2", "pic2", "e2@mail", 
                new ArrayList<>(), 12345L, Gender.OTHER, "Loc2", "pwd2", 
                "Desc2", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        Event event4 = new Event(1L, user2, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event4);
        assertNotEquals(event1.hashCode(), event4.hashCode());
        
        // Diferente título
        Event event5 = new Event(1L, user, "Different Title", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event5);
        assertNotEquals(event1.hashCode(), event5.hashCode());
        
        // Diferente descripción
        Event event6 = new Event(1L, user, "Test Event", "Different Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event6);
        assertNotEquals(event1.hashCode(), event6.hashCode());
        
        // Diferente ubicación
        Event event7 = new Event(1L, user, "Test Event", "Event Description", "Different Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event7);
        assertNotEquals(event1.hashCode(), event7.hashCode());
        
        // Diferente fechaInicio/eventDate
        Event event8 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                2000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event8);
        assertNotEquals(event1.hashCode(), event8.hashCode());
        
        // Diferente fechaFin
        Event event9 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 2000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event9);
        assertNotEquals(event1.hashCode(), event9.hashCode());
        
        // Diferentes imágenes
        Event event10 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("different.jpg"), 
                new HashSet<>(), new ArrayList<>());
        
        assertNotEquals(event1, event10);
        assertNotEquals(event1.hashCode(), event10.hashCode());
        
        // Diferentes participantes
        Set<String> participantes = new HashSet<>();
        participantes.add("user1");
        Event event11 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                participantes, new ArrayList<>());
        
        assertNotEquals(event1, event11);
        assertNotEquals(event1.hashCode(), event11.hashCode());
        
        // Diferentes comentarios
        Comment comment = new Comment("test", user, null, 123L);
        List<Comment> comentarios = new ArrayList<>();
        comentarios.add(comment);
        Event event12 = new Event(1L, user, "Test Event", "Event Description", "Test Location", 
                1000000000L, 1000001000L, Arrays.asList("image1.jpg", "image2.jpg"), 
                new HashSet<>(), comentarios);
        
        assertNotEquals(event1, event12);
        assertNotEquals(event1.hashCode(), event12.hashCode());
    }

    @Test
    public void testToString() {
        String expected = "Event [id=1, creador=" + user.getUsername() + ", titulo=Test Event" + 
                ", descripcion=Event Description, ubicacion=Test Location" + 
                ", fechaInicio=1000000000, fechaFin=1000001000" + 
                ", eventDate=1000000000" +
                ", imagenes=[image1.jpg, image2.jpg], participantes=[], comentarios=[]]";
        assertEquals(expected, event.toString());
    }

    @Test
    public void testEmptyConstructor() {
        Event emptyEvent = new Event();
        assertNotNull(emptyEvent);
        assertNotNull(emptyEvent.getImagenes());
        assertTrue(emptyEvent.getImagenes().isEmpty());
        assertNotNull(emptyEvent.getParticipantes());
        assertTrue(emptyEvent.getParticipantes().isEmpty());
        assertNotNull(emptyEvent.getComentarios());
        assertTrue(emptyEvent.getComentarios().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        User testUser = new User("testUser", "Test Name", "testPic", "test@mail", 
                new ArrayList<>(), 12345L, Gender.OTHER, "Test Location", "testPwd", 
                "Test Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        List<String> images = Arrays.asList("img1.jpg", "img2.jpg");
        Set<String> participants = new HashSet<>();
        participants.add("user1");
        participants.add("user2");
        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment("test comment", testUser, null, 12345L);
        comments.add(comment);
        
        Event testEvent = new Event(5L, testUser, "New Event", "New Description", "New Location", 
                5000000000L, 5000001000L, images, participants, comments);
        
        assertNotNull(testEvent);
        assertEquals(5L, testEvent.getId());
        assertEquals(testUser, testEvent.getCreador());
        assertEquals("New Event", testEvent.getTitulo());
        assertEquals("New Description", testEvent.getDescripcion());
        assertEquals("New Location", testEvent.getUbicacion());
        assertEquals(5000000000L, testEvent.getFechaInicio());
        assertEquals(5000001000L, testEvent.getFechaFin());
        assertEquals(5000000000L, testEvent.getEventDate());
        assertEquals(2, testEvent.getImagenes().size());
        assertTrue(testEvent.getImagenes().contains("img1.jpg"));
        assertTrue(testEvent.getImagenes().contains("img2.jpg"));
        assertEquals(2, testEvent.getParticipantes().size());
        assertTrue(testEvent.getParticipantes().contains("user1"));
        assertTrue(testEvent.getParticipantes().contains("user2"));
        assertEquals(1, testEvent.getComentarios().size());
        assertTrue(testEvent.getComentarios().contains(comment));
    }
}