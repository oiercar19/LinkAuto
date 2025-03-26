package com.linkauto.restapi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.linkauto.restapi.model.User;
import com.linkauto.restapi.repository.UserRepository;

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
}
