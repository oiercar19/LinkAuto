package com.linkauto.restapi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.UserRepository;

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
    
    private static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }

    public boolean updateUser(User user, String token) {
        tokenStore.put(token, user);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean deleteUser(User user, String token) {
        try {
            // Eliminar los posts explícitamente
            user.getPosts().clear(); // Esto activa orphanRemoval

            // Eliminar followers y following si hace falta (para evitar foreign key conflicts)
            user.getFollowers().clear();
            user.getFollowing().clear();

            // Eliminar el usuario del repositorio
            System.out.println("Voy a borrar");
            userRepository.delete(user);
            System.out.println("Usuario borrado");
            
            System.out.println("Token store before deletion: " + tokenStore);
            // Eliminar el token asociado al usuario del tokenStore
            tokenStore.entrySet().removeIf(entry -> entry.getValue().equals(user) && entry.getKey().equals(token));
            System.out.println("Token store after deletion: " + tokenStore);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar el usuario o el token: " + e.getMessage());
            e.printStackTrace();  // Imprime el stack trace para depuración
            return false;
        }
    }
}
