package com.linkauto.restapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.EventDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.model.Comment;
import com.linkauto.restapi.model.Event;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.CommentRepository;
import com.linkauto.restapi.repository.EventRepository;
import com.linkauto.restapi.repository.PostRepository;
import com.linkauto.restapi.repository.UserRepository;

@Service
public class LinkAutoService {
    @Autowired
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;

    public LinkAutoService(PostRepository postRepository, UserRepository userRepository, 
                          CommentRepository commentRepository, EventRepository eventRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
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
            postUser.getSavedPosts().remove(post); 
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

    @Transactional
    public Boolean followUser(String followerUsername, String followedUsername) {
        if (followerUsername.equals(followedUsername)) {
            return false;
        }

        User follower = userRepository.findByUsername(followerUsername).orElse(null);
        User followed = userRepository.findByUsername(followedUsername).orElse(null);

        if (follower == null || followed == null) {
            return false;
        }

        if (!followed.getFollowers().contains(follower)) {
            followed.getFollowers().add(follower);
            follower.getFollowing().add(followed);
        }

        // Solo guardar uno, Hibernate lo propaga si tienes cascade configurado
        // O guarda ambos si quieres asegurarte
        userRepository.save(followed);
        userRepository.save(follower);

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

    @Transactional
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

    @Transactional
    public Boolean savePost (Long postId, User u) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return false;
    
        // Recupera el usuario desde DB para que tenga su Set<Post> sincronizado
        User user = userRepository.findByUsername(u.getUsername()).orElse(null);
        if (user == null) return false;
    
        boolean added = user.getSavedPosts().add(post); // Evita duplicados si ya existe en Set
        if (!added) return false; // Ya estaba guardado
    
        userRepository.save(user);
        return true;
    }

    @Transactional
    public Boolean unsavePost (Long postId, User u) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return false;
    
        User user = userRepository.findByUsername(u.getUsername()).orElse(null);
        // Buscar el post a eliminar dentro del Set de guardados
        Post postToRemove = user.getSavedPosts().stream()
            .filter(p -> p.getId().equals(postId))
            .findFirst()
            .orElse(null);
    
        if (postToRemove != null) {
            user.getSavedPosts().remove(postToRemove);
            userRepository.save(user);
            userRepository.flush();
            return true;
        }
    
        return false;
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
    
    // Event-related methods
    
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }
    
    public Event createEvent(EventDTO eventDTO, User user) {
        Event event = new Event();
        event.setTitulo(eventDTO.getTitle());
        event.setDescripcion(eventDTO.getDescription());
        event.setUbicacion(eventDTO.getLocation());
        event.setFechaInicio(eventDTO.getStartDate());
        event.setFechaFin(eventDTO.getEndDate());
        event.setCreador(user);
        
        // Añadir imágenes
        if (eventDTO.getImages() != null) {
            for (String imagen : eventDTO.getImages()) {
                event.addImagen(imagen);
            }
        }
        
        eventRepository.save(event);
        
        return event;
    }
    
    @Transactional
    public boolean deleteEvent(Long id, User user) {
        try {
            // Buscar el evento
            Event event = eventRepository.findById(id)
                .orElse(null);
            
            // Verificar si el evento existe
            if (event == null) {
                return false;
            }
            
            // Verificar si el usuario tiene permiso (creador o admin)
            if (event.getCreador() == null || 
                !event.getCreador().getUsername().equals(user.getUsername())) {
                return false;
            }
            
            // Eliminar evento
            eventRepository.delete(event);
            eventRepository.flush();
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar evento: " + e.getMessage());
            return false;
        }
    }
    
    public Boolean participateInEvent(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return false;
        }
        
        // Asumiendo que necesitamos guardar el username como participante
        event.addParticipante(user.getUsername());
        eventRepository.save(event);
        
        return true;
    }
    
    public Boolean cancelParticipation(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return false;
        }
        
        event.removeParticipante(user.getUsername());
        eventRepository.save(event);
        
        return true;
    }
    
    public Set<String> getEventParticipants(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return null;
        }
        return event.getParticipantes();
    }
    
    public List<Event> getEventsByCreador(String username) {
        return eventRepository.findByCreador_Username(username);
    }

    public Boolean verifyUser(User user) {
        // Comprobar si tiene al menos 3 posts y 3 seguidores
        if (user.getPosts().size() < 3 || user.getFollowers().size() < 3) {
            return false;
        }
        user.setIsVerified(true);
        userRepository.save(user);
        return true;
    }
    
    public List<Post> getSavedPostsByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }
        return new ArrayList<>(user.getSavedPosts());
    }

    
    public Boolean commentEvent(Long eventId, User user, CommentDTO commentDTO) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return false;
        }
        
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setText(commentDTO.getText());
        comment.setEvent(event); // Asumiendo que la clase Comment tiene un campo para Event
        comment.setCreationDate(System.currentTimeMillis());
        
        commentRepository.save(comment);
        event.addComentario(comment);
        eventRepository.save(event);
        
        return true;
    }
    
    public List<Comment> getCommentsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return null;
        }
        return event.getComentarios();
    }
    
    // Método adicional para obtener los eventos en los que participa un usuario
    public List<Event> getUserParticipatingEvents(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return eventRepository.findByParticipantesContaining(username);
    }
    
    /**
     * Gets all events created by or participated in by a user
     * @param username The username of the user
     * @return List of events the user created or is participating in
     */
    public List<Event> getEventsByUsername(String username) {
        // This should combine both created events and participating events
        List<Event> createdEvents = eventRepository.findByCreador_Username(username);
        List<Event> participatingEvents = eventRepository.findByParticipantesContaining(username);
        
        // Add all participating events not already in created events
        for (Event event : participatingEvents) {
            if (!createdEvents.contains(event)) {
                createdEvents.add(event);
            }
        }
        
        return createdEvents;
    }
    
    /**
     * Updates an existing event
     * @param id The ID of the event to update
     * @param eventDTO The new event data
     * @param user The user attempting to update the event
     * @return Optional containing the updated event if successful, empty otherwise
     */
    public Optional<Event> updateEvent(Long id, EventDTO eventDTO, User user) {
        // Get the event
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            return Optional.empty();
        }
        
        Event event = eventOptional.get();
        
        // Check if the user is the creator of the event
        if (!event.getCreador().getUsername().equals(user.getUsername())) {
            return Optional.empty();
        }
        
        // Update the event details
        event.setTitulo(eventDTO.getTitle());
        event.setDescripcion(eventDTO.getDescription());
        event.setUbicacion(eventDTO.getLocation());
        event.setFechaInicio(eventDTO.getStartDate());
        event.setFechaFin(eventDTO.getEndDate());
        
        // Update images if provided
        if (eventDTO.getImages() != null) {
            // Clear existing images and add new ones
            event.getImagenes().clear();
            for (String imagen : eventDTO.getImages()) {
                event.addImagen(imagen);
            }
        }
        
        // Save the updated event
        Event updatedEvent = eventRepository.save(event);
        return Optional.of(updatedEvent);
    }
}
