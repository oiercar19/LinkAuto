package com.linkauto.restapi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.UserRepository;
import com.linkauto.restapi.model.Role; // Ensure Role is imported from the correct package

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final static Map<String, User> tokenStore = new HashMap<>();

    public boolean register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return false;
        }
        userRepository.save(user);
        return true;
    }

    public String login(String username, String password) {
        User user = userRepository.findById(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        String token = generateToken();
        tokenStore.put(token, user);
        return token;
    }

    public boolean logout(String token) {
        if (tokenStore.containsKey(token)) {
            tokenStore.remove(token);
            return true;
        } else {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        return tokenStore.containsKey(token);
    }

    public User getUserByToken(String token) {
        return tokenStore.get(token);
    }

    public User getUserByUsername(String username) {
        return userRepository.findById(username).orElse(null);
    }
    
    private static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }

    public boolean updateUser(User user, String token) {
        tokenStore.put(token, user);
        userRepository.save(user);
        return true;
    }

    public boolean changeRole(User user, Role role) {
        user.setRole(role);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean deleteUser(User user, String token) {
        try {
            if (!tokenStore.containsKey(token)) {
                return false;
                
            }

            if (!tokenStore.get(token).getUsername().equals(user.getUsername()) && !tokenStore.get(token).getRole().equals(Role.ADMIN)) {
                return false;
            }
            // Eliminar los posts explícitamente
            user.getPosts().forEach(post -> {
                post.setUsuario(null);
            });
            user.getPosts().clear();

            // Eliminar followers y following si hace falta (para evitar foreign key conflicts)
            user.getFollowers().forEach(follower -> follower.getFollowing().remove(user));
            user.getFollowers().clear();
            user.getFollowing().forEach(following -> following.getFollowers().remove(user));
            user.getFollowing().clear();

            // Eliminar el usuario del repositorio
            userRepository.delete(user);
            
            // Eliminar el token asociado al usuario del tokenStore
            tokenStore.remove(token);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar el usuario o el token: " + e.getMessage());
            e.printStackTrace();  // Imprime el stack trace para depuración
            return false;
        }
    }
}
