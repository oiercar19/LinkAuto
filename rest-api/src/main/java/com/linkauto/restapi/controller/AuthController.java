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
import com.linkauto.restapi.model.Role;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.service.AuthService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "LinkAuto authentication controller", description = "Register, login and logout operations")
public class AuthController {
    
    /**
     * The AuthService instance used to handle authentication-related operations.
     * This field is automatically injected by the Spring framework.
     */
    @Autowired
    private final AuthService authService;

    /**
     * Constructs an instance of AuthController with the specified AuthService.
     *
     * @param authService the service used for handling authentication-related operations
     */
    public AuthController(AuthService authService) {
        this.authService = authService;  
    }

    /**
     * Handles the registration of a new user.
     *
     * @param user The user registration data encapsulated in a {@link UserRegisterDTO}.
     * @return A {@link ResponseEntity} with:
     *         - {@link HttpStatus#CREATED} if the registration is successful.
     *         - {@link HttpStatus#BAD_REQUEST} if the registration fails.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserRegisterDTO user) {
        User u = parseUserRegisterDTOToUser(user);
        boolean resultado = authService.register(u);
        if (!resultado) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);   
    }
    
    /**
     * Handles the login request for a user.
     *
     * @param credenciales the credentials of the user, containing username and password.
     * @return a ResponseEntity containing the authentication token if login is successful,
     *         or an HTTP 401 Unauthorized status if the login fails.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody CredencialesDTO credenciales) {
        String token = authService.login(credenciales.getUsuario(), credenciales.getContrasena());
        if (token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    /**
     * Handles the logout operation for a user.
     *
     * @param userToken The token of the user attempting to log out. 
     *                  This parameter is required and should be provided 
     *                  as a request parameter. Example: "1234567890".
     * @return A ResponseEntity with HTTP status:
     *         - 200 (OK) if the logout operation is successful.
     *         - 401 (UNAUTHORIZED) if the logout operation fails.
     */
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
        
    /**
     * Converts a UserRegisterDTO object to a User object.
     *
     * @param userRegisterDTO the data transfer object containing user registration details
     * @return a User object populated with the data from the UserRegisterDTO
     * @throws IllegalArgumentException if the gender or role provided in the DTO
     *                                  does not match the expected enum values
     */
    public User parseUserRegisterDTOToUser(UserRegisterDTO userRegisterDTO) {
        User user = new User(
            userRegisterDTO.getUsername(),
            userRegisterDTO.getName(),
            userRegisterDTO.getProfilePicture(),
            userRegisterDTO.getEmail(),
            userRegisterDTO.getCars(),
            userRegisterDTO.getBirthDate(),
            Gender.valueOf(userRegisterDTO.getGender()),
            userRegisterDTO.getLocation(),
            userRegisterDTO.getPassword(),
            userRegisterDTO.getDescription(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        user.setRole(Role.valueOf(userRegisterDTO.getRole().toUpperCase()));
        return user;   
    }
    
}

