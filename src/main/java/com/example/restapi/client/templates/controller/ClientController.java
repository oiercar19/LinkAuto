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
        
        model.addAttribute("user", user);
        
        return "editarPerfil";
    }
    
    @PostMapping("/editProfile")
    public String updateProfile(User updatedUser, RedirectAttributes redirectAttributes, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || sessionUser.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/editarPerfil";
        }
        
        try {
            // Preserve the username and ID from the session user
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
            List<Post> posts = linkAutoServiceProxy.getFeed(user.getUsername());
            model.addAttribute("posts", posts);
            
            return "feed";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load feed: " + e.getMessage());
            return "feed";
        }
    }
    
    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable int userId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/profile/" + userId;
        }
        
        try {
            User profileUser = linkAutoServiceProxy.getUserProfile(user.getUsername(), userId);
            List<Post> userPosts = linkAutoServiceProxy.getUserPosts(user.getUsername(), userId);
            
            model.addAttribute("profileUser", profileUser);
            model.addAttribute("userPosts", userPosts);
            
            return "userProfile";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load profile: " + e.getMessage());
            return "redirect:/feed";
        }
    }
    
    @PostMapping("/follow/{followeeId}")
    public String followUser(@PathVariable int followeeId, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/profile/" + followeeId;
        }
        
        try {
            linkAutoServiceProxy.followUser(user.getUsername(), user.getUsername(), followeeId);
            redirectAttributes.addFlashAttribute("message", "User followed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to follow user: " + e.getMessage());
        }
        
        return "redirect:/profile/" + followeeId;
    }
    
    @PostMapping("/unfollow/{followeeId}")
    public String unfollowUser(@PathVariable int followeeId, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/profile/" + followeeId;
        }
        
        try {
            linkAutoServiceProxy.unfollowUser(user.getUsername(), user.getUsername(), followeeId);
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
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.likePost(user.getUsername(), user.getUsername(), postId);
            redirectAttributes.addFlashAttribute("message", "Post liked successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to like post: " + e.getMessage());
        }
        
        // Redireccionar a la URL de origen o al feed por defecto
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }

    @PostMapping("/unlike/{postId}")
    public String unlikePost(@PathVariable int postId, 
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.unlikePost(user.getUsername(), postId);
            redirectAttributes.addFlashAttribute("message", "Post unliked successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unlike post: " + e.getMessage());
        }
        
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }
    
    @PostMapping("/comment/{postId}")
    public String commentPost(@PathVariable int postId, 
            @RequestParam("comment") String comment,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.commentOnPost(user.getUsername(), user.getUsername(), postId, comment);
            redirectAttributes.addFlashAttribute("message", "Comment added successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add comment: " + e.getMessage());
        }
        
        // Redireccionar a la URL de origen o al feed por defecto
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }

    @PostMapping("/deleteComment/{postId}/{commentId}")
    public String deleteComment(@PathVariable int postId, 
            @PathVariable int commentId,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion";
        }
        
        try {
            linkAutoServiceProxy.deleteComment(user.getUsername(), postId, commentId);
            redirectAttributes.addFlashAttribute("message", "Comment deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete comment: " + e.getMessage());
        }
        
        return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/feed");
    }
    
    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getUsername() == null) {
            return "redirect:/inicioSesion?redirectUrl=/search?query=" + query;
        }
        
        try {
            List<User> users = linkAutoServiceProxy.searchUsers(user.getUsername(), query);
            List<Post> posts = linkAutoServiceProxy.searchPosts(user.getUsername(), query);
            
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

}