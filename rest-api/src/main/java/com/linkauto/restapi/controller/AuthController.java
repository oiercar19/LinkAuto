package com.linkauto.restapi.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkauto.restapi.dto.CredencialesDTO;
import com.linkauto.restapi.dto.UserRegisterDTO;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.service.AuthService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "LinkAuto authentication controller", description = "Register, login and logout operations")
public class AuthController {
    
    @Autowired
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;  
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserRegisterDTO user) {
        User u = parseUserRegisterDTOToUser(user);
        boolean resultado = authService.register(u);
        if (!resultado) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);   
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody CredencialesDTO credenciales) {
        String token = authService.login(credenciales.getUsuario(), credenciales.getContrasena());
        if (token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(        
    @Parameter(name = "userToken", description = "Token of the user", required = true, example = "1234567890")
    @RequestParam("userToken") String userToken) {
        boolean resultado = authService.logout(userToken);
        if (!resultado) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
        
    public User parseUserRegisterDTOToUser(UserRegisterDTO userRegisterDTO) {
        User user = new User(userRegisterDTO.getUsername(), userRegisterDTO.getName(), userRegisterDTO.getProfilePicture(), userRegisterDTO.getEmail(), userRegisterDTO.getCars(), userRegisterDTO.getBirthDate(), Gender.valueOf(userRegisterDTO.getGender()), userRegisterDTO.getLocation(), userRegisterDTO.getPassword(), userRegisterDTO.getDescription(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        return user;   
    }
}

