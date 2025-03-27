package com.linkauto.client.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linkauto.client.data.Credenciales;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.User;
import com.linkauto.client.service.ClientServiceProxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ClientController {

    @Autowired
    private ClientServiceProxy linkAutoServiceProxy;

    private String token;
    private User loggedUser;
    private String userName;

    // Add current URL and username to all views
    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
        model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
        model.addAttribute("token", token); // Makes token available in all templates
        model.addAttribute("loggedUser", loggedUser); // Makes logged user available in all templates
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("username", userName);
        return "index";
    }

    @GetMapping("/registroUsuario")
    public String showRegister(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            Model model) {
        model.addAttribute("redirectUrl", redirectUrl);
        return "registroUsuario";
    }
    
    @PostMapping("/register")
    public String performRegister(
            @RequestParam("username") String username,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "profilePictureUrl", required = false) String profilePicture,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "cars", required = false) List<String> cars,
            RedirectAttributes redirectAttributes) {
            System.out.println("Registering user: " + username);
        try {
            // Create a new User record with the provided parameters

            Long birthDateTimestamp = null;
            if (birthDate != null && !birthDate.isEmpty()) {
                try {
                    LocalDate birthDateParsed = LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE);
                    birthDateTimestamp = birthDateParsed.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                } catch (DateTimeParseException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Invalid birth date format. Use yyyy-MM-dd.");
                    return "redirect:/registroUsuario";
                }
            }

            String genderFormatted = (gender != null) ? gender.toUpperCase() : null;

            User user = new User(
                username, 
                name, 
                profilePicture, 
                email, 
                cars, // cars list 
                birthDateTimestamp, 
                genderFormatted, 
                location, 
                password, 
                description, 
                null // posts list
            );

            // Call service to register the user
            linkAutoServiceProxy.register(user);

            // If successful, redirect to login page
            redirectAttributes.addFlashAttribute("message", "User registered successfully");
            return "redirect:/inicioSesion"; 

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error during registration: " + e.getMessage());
            return "redirect:/registroUsuario"; 
        }
    }
    
    @GetMapping("/inicioSesion")
    public String showLoginPage(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            Model model) {
        // Add redirectUrl to the model if needed
        model.addAttribute("redirectUrl", redirectUrl);

        return "inicioSesion"; 
    }

    @PostMapping("/login")
    public String performLogin(@RequestParam("username") String username, @RequestParam("password") String password,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl, 
            Model model) {
        // Create Credenciales record with the correct parameter names
        Credenciales credentials = new Credenciales(username, password);
        try {
            // Call service to authenticate user
            token = linkAutoServiceProxy.login(credentials);
            loggedUser = linkAutoServiceProxy.getUserProfile(token);
            System.out.println("Logged in as: " + username);
            userName = credentials.usuario();
            // Redirect to the original page or root if redirectUrl is null
            
			return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/");
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "inicioSesion"; // Return to login page with error message
        }
    }

    @GetMapping("/logout")
    public String performLogout(HttpSession session,
            @RequestParam(value = "redirectUrl", defaultValue = "/") String redirectUrl,
            Model model) {
        try {
            linkAutoServiceProxy.logout(token);
            token = null;
            // Clear session
            System.out.println("Logged out");
            session.invalidate();
            
            model.addAttribute("successMessage", "Logout successful.");
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Logout failed: " + e.getMessage());
        }

        // Redirect to the specified URL after logout
        return "redirect:" + redirectUrl;
    }
    
    @GetMapping("/editarPerfil")
    public String showEditProfile(Model model, HttpSession session) {
        if (token == null) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        // Get the full user profile in case additional details are needed
        User fullProfile = linkAutoServiceProxy.getUserProfile(token);
        model.addAttribute("user", fullProfile);
        
        return "editarPerfil";
    }
    
    @PostMapping("/editProfile")
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam(value = "profilePicture", required = false) String profilePicture,
            @RequestParam("email") String email,
            @RequestParam(value = "birthDate", required = false) Long birthDate,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes, 
            HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || sessionUser.username() == null) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        
        try {
            // Create an updated user record 
            User updatedUser = new User(
                sessionUser.username(), 
                name, 
                profilePicture, 
                email, 
                sessionUser.cars(), // preserve existing cars list
                birthDate != null ? birthDate : sessionUser.birthDate(), 
                gender, 
                location, 
                sessionUser.password(), // preserve existing password 
                description, 
                sessionUser.posts() // preserve existing posts list
            );
            
            // Update user profile
            linkAutoServiceProxy.updateProfile(sessionUser.username(), updatedUser);
            
            // Update session with new user data
            session.setAttribute("user", updatedUser);
            
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            return "redirect:/editarPerfil";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            return "redirect:/editarPerfil";
        }
    }
    
    @GetMapping("/subirPosts")
    public String showCreatePost(Model model, HttpSession session) {
        if (token == null) {
            return "redirect:/inicioSesion?redirectUrl=/subirPosts";
        }
        return "subirPosts";
    }
    
    @PostMapping("/uploadPost")
    public String uploadPost(
            @RequestParam("mensaje") String mensaje,
            @RequestParam(value = "imagenes", required = false) List<String> imagenes,
            RedirectAttributes redirectAttributes, 
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.username() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            // Create a new Post record
            Post post = new Post(
                0, // id will be assigned by the backend
                user, 
                mensaje, 
                System.currentTimeMillis(), 
                imagenes
            );
            
            linkAutoServiceProxy.createPost(user.username(), post);
            
            redirectAttributes.addFlashAttribute("message", "Post created successfully");
            return "redirect:/feed";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create post: " + e.getMessage());
            return "redirect:/subirPosts";
        }
    }

    @PostMapping("/deletePost/{postId}")
    public String deletePost(@PathVariable int postId, 
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.username() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.deletePost(user.username(), postId);
            redirectAttributes.addFlashAttribute("message", "Post deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete post: " + e.getMessage());
        }
        
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }
    
    @GetMapping("/feed")
    public String showFeed(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.username() == null) {
            return "redirect:/inicioSesion?redirectUrl=/feed";
        }
        
        try {
            List<Post> posts = linkAutoServiceProxy.getFeed();
            model.addAttribute("posts", posts);
            
            return "feed";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load feed: " + e.getMessage());
            return "feed";
        }
    }
}