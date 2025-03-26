package com.linkauto.restapi.client.controller;

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

import com.linkauto.restapi.model.Post;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.client.service.ClientServiceProxy;
import com.linkauto.restapi.model.CredencialesDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ClientController {

    @Autowired
    private ClientServiceProxy linkAutoServiceProxy;

    // Add current URL and username to all views
    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request, HttpSession session) {
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
        model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
        
        // Get user data from session if available
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername()); // Makes username available in all templates
            model.addAttribute("isLoggedIn", true); // Flag for templates to know if user is logged in
        } else {
            model.addAttribute("isLoggedIn", false);
        }
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
            Model model, HttpSession session) {
        // Crear objeto de credenciales con los nombres correctos esperados por el backend
        CredencialesDTO credentials = new CredencialesDTO(username, password);

        try {
            // Call service to authenticate user
            User user = linkAutoServiceProxy.login(credentials);
            
            // Store user information in session
            session.setAttribute("user", user);
            
            // Redirect to the original page or root if redirectUrl is null
            return "redirect:/index" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/");
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
            // Call service to logout if user exists in session
            User user = (User) session.getAttribute("user");
            if (user != null && user.getUsername() != null) {
                linkAutoServiceProxy.logout(user.getUsername());
            }
            
            // Clear session
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
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        
        // Get the full user profile in case additional details are needed
        User fullProfile = linkAutoServiceProxy.getUserProfile(user.getUsername());
        model.addAttribute("user", fullProfile);
        
        return "editarPerfil";
    }
    
    @PostMapping("/editProfile")
    public String updateProfile(User updatedUser, RedirectAttributes redirectAttributes, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || sessionUser.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        
        try {
            // Preserve the username from the session user
            updatedUser.setUsername(sessionUser.getUsername());
            
            // Update user profile
            linkAutoServiceProxy.updateProfile(sessionUser.getUsername(), updatedUser);
            
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
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/subirPosts";
        }
        
        return "subirPosts";
    }
    
    @PostMapping("/uploadPost")
    public String uploadPost(Post post, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            post.setUsuario(user);
            post.setFechaCreacion(System.currentTimeMillis());
            linkAutoServiceProxy.createPost(user.getUsername(), post);
            
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
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.deletePost(user.getUsername(), postId);
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
        if (user == null || user.getUsername() == null) {
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