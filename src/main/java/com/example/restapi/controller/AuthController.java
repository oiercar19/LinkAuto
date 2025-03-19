package com.example.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.model.User;
import com.example.restapi.service.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "LinkAuto authentication controller", description = "Register, login and logout operations")
public class AuthController {
    
        @Autowired
        private AuthService authService;

        public AuthController(AuthService authService) {
            this.authService = authService;  
        }
    
        @PostMapping("/register")
        public ResponseEntity<Void> register(@RequestBody User user) {
            boolean resultado = authService.register(user);
            if (!resultado) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
            
	}
}

