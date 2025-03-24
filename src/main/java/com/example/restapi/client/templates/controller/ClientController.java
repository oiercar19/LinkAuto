/**
 * Controller for the LinkAuto social network web application.
 * This controller handles web requests and coordinates with services.
 */
package com.example.restapi.client.templates.controller;

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

import com.example.restapi.model.Post;
import com.example.restapi.model.User;
import com.example.restapi.model.CredencialesDTO;
import com.example.restapi.client.templates.service.ClientServiceProxy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ClientController {

    @Autowired
    private ClientServiceProxy linkAutoServiceProxy;

    private String token; // Stores the session token
    private int userId;

    // Add current URL and token to all views
    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
        model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
        model.addAttribute("token", token); // Makes token available in all templates
        model.addAttribute("userId", userId); // Makes userId available in all templates
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/registroUsuario")
    public String showRegister(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            Model model) {
        model.addAttribute("redirectUrl", redirectUrl);
        return "registroUsuario";
    }
    
    @PostMapping("/register")
    public String performRegister(User user, RedirectAttributes redirectAttributes) {
        try {
            // Call service to register the user
            linkAutoServiceProxy.register(user);

            // If successful, redirect to a success page or to the user's profile
            redirectAttributes.addFlashAttribute("message", "User registered successfully");
            return "redirect:/inicioSesion"; 

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error during registration");
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
            Model model, HttpSession session) {
        // Crear objeto de credenciales con los nombres correctos esperados por el backend
        CredencialesDTO credentials = new CredencialesDTO(username, password);

        try {
            // Call service to authenticate user
            User user = linkAutoServiceProxy.login(credentials);
            
            // Store user information in session
            token = "user-token-" + user.getUsername(); // In a real app, this would be from the service
            
            // Guardar el userId - asumiendo que el username se puede convertir a entero 
            // o idealmente usar un campo ID del objeto User si estuviera disponible
            try {
                userId = Integer.parseInt(user.getUsername());
            } catch (NumberFormatException e) {
                // Si el username no es un número, asignar un valor temporal
                // En una aplicación real, el objeto User debería tener un ID numérico separado
                userId = user.getUsername().hashCode();
            }
            
            session.setAttribute("user", user);
            session.setAttribute("token", token);
            session.setAttribute("userId", userId);

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
            // Call service to logout
            if (token != null) {
                linkAutoServiceProxy.logout(token);
            }
            
            // Clear session
            token = null;
            userId = 0;
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
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        
        return "editarPerfil";
    }
    
    @PostMapping("/editProfile")
    public String updateProfile(User user, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        
        try {
            linkAutoServiceProxy.updateProfile(token, userId, user);
            
            // Update session with new user data
            session.setAttribute("user", user);
            
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            return "redirect:/editarPerfil";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            return "redirect:/editarPerfil";
        }
    }
    
    @GetMapping("/subirPosts")
    public String showCreatePost(Model model) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/subirPosts";
        }
        
        return "subirPosts";
    }
    
    @PostMapping("/uploadPost")
    public String uploadPost(Post post, RedirectAttributes redirectAttributes) {
        if (!isLogged()) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.createPost(token, userId, post);
            
            redirectAttributes.addFlashAttribute("message", "Post created successfully");
            return "redirect:/feed";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create post: " + e.getMessage());
            return "redirect:/subirPosts";
        }
    }
    
    @GetMapping("/feed")
    public String showFeed(Model model) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/feed";
        }
        
        try {
            List<Post> posts = linkAutoServiceProxy.getFeed(token, userId);
            model.addAttribute("posts", posts);
            
            return "feed";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load feed: " + e.getMessage());
            return "feed";
        }
    }
    
    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable int userId, Model model) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/profile/" + userId;
        }
        
        try {
            User user = linkAutoServiceProxy.getUserProfile(token, userId);
            List<Post> userPosts = linkAutoServiceProxy.getUserPosts(token, userId);
            
            model.addAttribute("profileUser", user);
            model.addAttribute("userPosts", userPosts);
            
            return "userProfile";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load profile: " + e.getMessage());
            return "redirect:/feed";
        }
    }
    
    // Nuevos endpoints para interacciones sociales
    
    @PostMapping("/follow/{followeeId}")
    public String followUser(@PathVariable int followeeId, RedirectAttributes redirectAttributes) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/profile/" + followeeId;
        }
        
        try {
            linkAutoServiceProxy.followUser(token, userId, followeeId);
            redirectAttributes.addFlashAttribute("message", "User followed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to follow user: " + e.getMessage());
        }
        
        return "redirect:/profile/" + followeeId;
    }
    
    @PostMapping("/unfollow/{followeeId}")
    public String unfollowUser(@PathVariable int followeeId, RedirectAttributes redirectAttributes) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/profile/" + followeeId;
        }
        
        try {
            linkAutoServiceProxy.unfollowUser(token, userId, followeeId);
            redirectAttributes.addFlashAttribute("message", "User unfollowed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unfollow user: " + e.getMessage());
        }
        
        return "redirect:/profile/" + followeeId;
    }
    
    @PostMapping("/like/{postId}")
    public String likePost(@PathVariable int postId, 
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {
        if (!isLogged()) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.likePost(token, userId, postId);
            redirectAttributes.addFlashAttribute("message", "Post liked successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to like post: " + e.getMessage());
        }
        
        // Redireccionar a la URL de origen o al feed por defecto
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }
    
    @PostMapping("/comment/{postId}")
    public String commentPost(@PathVariable int postId, 
            @RequestParam("comment") String comment,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {
        if (!isLogged()) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.commentOnPost(token, userId, postId, comment);
            redirectAttributes.addFlashAttribute("message", "Comment added successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add comment: " + e.getMessage());
        }
        
        // Redireccionar a la URL de origen o al feed por defecto
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }
    
    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
        if (!isLogged()) {
            return "redirect:/inicioSesion?redirectUrl=/search?query=" + query;
        }
        
        try {
            List<User> users = linkAutoServiceProxy.searchUsers(token, query);
            List<Post> posts = linkAutoServiceProxy.searchPosts(token, query);
            
            model.addAttribute("searchQuery", query);
            model.addAttribute("users", users);
            model.addAttribute("posts", posts);
            
            return "searchResults";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Search failed: " + e.getMessage());
            return "searchResults";
        }
    }
    
    private boolean isLogged() {
        return token != null && userId != 0;
    }
}