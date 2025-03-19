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

}
