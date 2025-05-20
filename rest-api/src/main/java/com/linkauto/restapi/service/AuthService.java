package com.linkauto.restapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.EventRepository;
import com.linkauto.restapi.repository.PostRepository;
import com.linkauto.restapi.repository.UserRepository;
import com.linkauto.restapi.model.Event;
import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.Role; // Ensure Role is imported from the correct package

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    /**
     * Repository interface for performing CRUD operations on User entities.
     * This is used to interact with the database layer for user-related data.
     */
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PostRepository postRepository;


    /**
     * Constructs an instance of AuthService with the specified UserRepository.
     *
     * @param userRepository the repository used to manage user data
     */
    public AuthService(UserRepository userRepository, PostRepository postRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository; 
        this.postRepository = postRepository;
    }

    /**
     * A static map that serves as an in-memory token store for managing user authentication tokens.
     * The keys in the map represent authentication tokens (as Strings), and the values are User objects
     * associated with those tokens.
     *
     * This map is used to track and validate user sessions in the application.
     */
    private final static Map<String, User> tokenStore = new HashMap<>();

    /**
     * Registers a new user in the system.
     *
     * @param user The user object containing the details of the user to be registered.
     * @return {@code true} if the user was successfully registered; 
     *         {@code false} if a user with the same username already exists.
     */
    public boolean register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return false;
        }
        userRepository.save(user);
        return true;
    }

    /**
     * Authenticates a user by verifying the provided username and password.
     * If the credentials are valid, generates a token and stores it for the user.
     *
     * @param username the username of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return a generated token if authentication is successful, or {@code null} if authentication fails
     */
    public String login(String username, String password) {
        User user = userRepository.findById(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        String token = generateToken();
        tokenStore.put(token, user);
        return token;
    }

    /**
     * Logs out a user by invalidating the provided token.
     *
     * @param token The token to be invalidated.
     * @return {@code true} if the token was successfully removed from the token store,
     *         {@code false} if the token does not exist in the token store.
     */
    public boolean logout(String token) {
        if (tokenStore.containsKey(token)) {
            tokenStore.remove(token);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Validates if the provided token exists in the token store.
     *
     * @param token the token to be validated
     * @return {@code true} if the token exists in the token store, {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        return tokenStore.containsKey(token);
    }

    /**
     * Retrieves a User object associated with the given token.
     *
     * @param token the token used to identify the user
     * @return the User object associated with the token, or null if no user is found
     */
    public User getUserByToken(String token) {
        return tokenStore.get(token);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user to retrieve
     * @return the User object if found, or null if no user exists with the given username
     */
    public User getUserByUsername(String username) {
        return userRepository.findById(username).orElse(null);
    }
    
    /**
     * Generates a unique token based on the current system time in milliseconds.
     * The token is represented as a hexadecimal string.
     * 
     * This method is synchronized to ensure thread safety when accessed
     * concurrently by multiple threads.
     * 
     * @return A unique hexadecimal string token.
     */
    private static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }

    /**
     * Updates the user information and associates it with the provided token.
     *
     * @param user  The user object containing updated information to be saved.
     * @param token The token to associate with the user in the token store.
     * @return true if the operation is successful.
     */
    public boolean updateUser(User user, String token) {
        tokenStore.put(token, user);
        userRepository.save(user);
        return true;
    }

    /**
     * Changes the role of the specified user to the given role.
     *
     * @param user the user whose role is to be changed
     * @param role the new role to assign to the user
     * @return true if the role change was successful
     */
    public boolean changeRole(User user, Role role) {
        user.setRole(role);
        userRepository.save(user);
        return true;
    }

    /**
     * Deletes a user from the system along with their associated data, such as posts,
     * followers, and following relationships. This method also removes the user's token
     * from the token store to invalidate their session.
     *
     * @param user  The user to be deleted.
     * @param token The token associated with the user performing the deletion. This token
     *              is used to verify if the operation is authorized.
     * @return {@code true} if the user was successfully deleted; {@code false} otherwise.
     *         Returns {@code false} if the token is invalid, the token does not belong to
     *         the user being deleted, or the user performing the operation is not an admin.
     * @throws Exception If an unexpected error occurs during the deletion process.
     */
    @Transactional
    public boolean deleteUser(User user1, String token) {
        try {
            if (!tokenStore.containsKey(token)) {
                return false;
                
            }

            User user = userRepository.findById(user1.getUsername()).orElse(null);

            if (!tokenStore.get(token).getUsername().equals(user.getUsername()) && !tokenStore.get(token).getRole().equals(Role.ADMIN)) {
                return false;
            }
            // Eliminar los posts explícitamente
            user.getPosts().forEach(post -> {
                post.setUsuario(null);
            });
            user.getPosts().clear();

            List<Event> eventos = eventRepository.findByCreador_Username(user.getUsername());
            for (Event evento : eventos) {
                evento.setCreador(null);
                eventRepository.delete(evento);
            }

            for (Post savedPost : user.getSavedPosts()) {
                savedPost.setUsuario(null);
                postRepository.save(savedPost);  // <-- Aseguramos que el post quede persistido con el cambio
            }
            user.getSavedPosts().clear();
            userRepository.deleteSavedPostsByUsername(user.getUsername());

            user.getSavedPosts().forEach(savedPost -> savedPost.setUsuario(null));
            user.getSavedPosts().clear();

            // Eliminar followers y following si hace falta (para evitar foreign key conflicts)
            user.getFollowers().forEach(follower -> follower.getFollowing().remove(user));
            user.getFollowers().clear();
            user.getFollowing().forEach(following -> following.getFollowers().remove(user));
            user.getFollowing().clear();

            


            // Eliminar el usuario del repositorio
            userRepository.delete(user);
            
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar el usuario o el token: " + e.getMessage());
            e.printStackTrace();  // Imprime el stack trace para depuración
            return false;
        }
    }

    public boolean banUser(String username, boolean banStatus) {
        try {

            System.out.println("\n\n\n\nBan request received for username: " + username + ", banStatus: " + banStatus + "\\n" + //
                        "\n" + //
                        "\n" + //
                        "\n");
            User user = userRepository.findById(username).orElse(null);
            if (user == null) {
                return false; // Usuario no encontrado
            }
            user.setBanned(banStatus);
            userRepository.save(user);
            return true; // Usuario actualizado correctamente
        } catch (Exception e) {
            System.out.println("Error al actualizar el estado de baneo del usuario: " + e.getMessage());
            e.printStackTrace();
            return false; // Error al actualizar el usuario
        }
    }
}
