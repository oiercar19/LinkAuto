package com.example.restapi.service;

import java.util.HashMap;
import java.util.Map;

import com.example.restapi.model.User;
import com.example.restapi.repository.UserRepository;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static Map<String, User> tokenStore = new HashMap<>();

    public boolean register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return false;
        }
        userRepository.save(user);
        return true;
    }

    public boolean login(String username, String password) {
        User user = userRepository.findById(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return false;
        }
        String token = generateToken();
        tokenStore.put(token, user);
        return true;
    }

    public boolean logout(String token) {
        if (tokenStore.containsKey(token)) {
            tokenStore.remove(token);
            return true;
        } else {
            return false;
        }
    }
    
}
