package com.linkauto.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linkauto.client.data.Comment;
import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
import com.linkauto.client.data.User;
import com.linkauto.client.service.ClientServiceProxy;

import jakarta.servlet.http.HttpServletRequest;




@Controller
public class ClientController {

    @Autowired
    private ClientServiceProxy linkAutoServiceProxy;
	
    private String token; // Stores the session token
    private String username; // Stores the username

    public void addAttributes(Model model, HttpServletRequest request) {
		String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
		model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
	}

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/login")
    public String login (@RequestParam String username, @RequestParam String password, RedirectAttributes redirectAttributes, Model model) {
        try {
            Credentials credentials = new Credentials(username, password);
            token = linkAutoServiceProxy.login(credentials);
            this.username = linkAutoServiceProxy.getUserProfile(token).username();
            return "redirect:/feed";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas");
            return "redirect:/";
        }
    }
    
    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        linkAutoServiceProxy.logout(token);     
        token = null; // Clear the token after logout
        username = null; // Clear the username after logout
        return "redirect:/"; // Redirect to the home page after logout   
    }

    @GetMapping("/feed")
    public String feed(Model model) {
        // Verificar si el token proporcionado es válido
        if (token != null) {
            List<Post> posts = new ArrayList<>(linkAutoServiceProxy.getFeed());
            Map<String, String> profilePictureByUsername = new HashMap<>();
            for (Post post : posts) {
                String profilePicture = linkAutoServiceProxy.getUserByUsername(post.username()).profilePicture();
                profilePictureByUsername.putIfAbsent(post.username(), profilePicture);
            }
            
            model.addAttribute("profilePictureByUsername", profilePictureByUsername); // Agregar fotos de perfil al modelo
            
            model.addAttribute("posts", posts); // Agregar publicaciones al modelo
            model.addAttribute("username", username); // Agregar nombre de usuario al modelo
            String profilePicture = linkAutoServiceProxy.getUserProfile(token).profilePicture();
            model.addAttribute("profilePicture", profilePicture); // Agregar foto de perfil al modelo

            List<User> followings = linkAutoServiceProxy.getUserFollowing(username);
            List<String> followingUsernames = new ArrayList<>();
            for (User following : followings) {
                followingUsernames.add(following.username());
            }
            model.addAttribute("followings", followingUsernames); // Agregar seguidores al modelo
            return "feed"; // Vista para usuarios autenticados
        } else {
            // Token inválido o no proporcionado, redirigir al inicio de sesión
            return "redirect:/";
        }
    }

    @GetMapping("/register")
    public String showRegister(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
    Model model) {
        model.addAttribute("redirectUrl", redirectUrl);
        return "register"; // Vista de registro
    }

    @PostMapping("/register")
    public String performRegister(@RequestBody User u, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.register(u);
            redirectAttributes.addFlashAttribute("success", "Usuario registrado con éxito");
            return "redirect:/"; // Redirigir a la página de inicio después del registro exitoso
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el usuario: " + e.getMessage());
            return "redirect:/register"; // Redirigir a la página de registro en caso de error
        }
    }

    @GetMapping("/updateProfile")
    public String showUpdateProfile(Model model) {
        if (token != null) {
            User u = linkAutoServiceProxy.getUserProfile(token);
            model.addAttribute("user", u);
            return "editProfile"; 
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestBody User u, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.updateProfile(token, u);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado con éxito");
            return "redirect:/feed"; // Redirigir a la página de inicio después de actualizar el perfil
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/post")
    public String createPost(@RequestBody PostCreator post, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.createPost(token, post);
            redirectAttributes.addFlashAttribute("success", "Publicación creada con éxito");
            return "redirect:/feed"; // Redirigir a la página de inicio después de crear la publicación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la publicación: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/deletePost")
    public String deletePost(@RequestParam("id") Long id, @RequestParam(value = "redirectUrl", required = false) String redirectUrl, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.deletePost(token, id);
            redirectAttributes.addFlashAttribute("success", "Publicación eliminada con éxito");
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/"); // Redirigir a la página de inicio después de seguir al usuario    
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la publicación: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/user/{username}/follow")
    public String followUser(@PathVariable String username, @RequestParam(value = "redirectUrl", required = false) String redirectUrl, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.followUser(token, username);
            redirectAttributes.addFlashAttribute("success", "Siguiendo a " + username);
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/"); // Redirigir a la página de inicio después de seguir al usuario    
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al seguir al usuario: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/user/{username}/unfollow")
    public String unfollowUser(@PathVariable String username, @RequestParam(value = "redirectUrl", required = false) String redirectUrl, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.unfollowUser(token, username);
            redirectAttributes.addFlashAttribute("success", "Dejado de seguir a " + username);
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/"); // Redirigir a la página de inicio después de seguir al usuario    
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al dejar de seguir al usuario: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @GetMapping("/user/{username}")
    public String userProfile(@PathVariable String username, Model model) {
        if (token != null) {
            User user = linkAutoServiceProxy.getUserByUsername(username);
            model.addAttribute("user", user); // Agregar usuario al modelo
            
            User currentUser = linkAutoServiceProxy.getUserProfile(token);
            model.addAttribute("currentUser", currentUser); // Agregar usuario al modelo
            
            List<Post> userPosts = new ArrayList<>(linkAutoServiceProxy.getUserPosts(username));
            model.addAttribute("userPosts", userPosts); // Agregar publicaciones al modelo

            Map<String, String> profilePictureByUsername = new HashMap<>();
            Map<Long, Comment> commentsByPostId = new HashMap<>();
            for (Post post : userPosts) {
                List<Comment> comments = linkAutoServiceProxy.getCommentsByPostId(post.id());
                
                for (Comment comment : comments) {
                    String profilePicture = linkAutoServiceProxy.getUserByUsername(comment.username()).profilePicture();
                    profilePictureByUsername.putIfAbsent(comment.username(), profilePicture);
                    
                    commentsByPostId.putIfAbsent(post.id(), comment);
                }
            }
            model.addAttribute("profilePictureByUsername", profilePictureByUsername); // Agregar fotos de perfil al modelo
            model.addAttribute("commentsByPostId", commentsByPostId); // Agregar comentarios al modelo
            
            List<User> followings = linkAutoServiceProxy.getUserFollowing(currentUser.username());
            List<String> followingUsernames = new ArrayList<>();
            for (User following : followings) {
                followingUsernames.add(following.username());
            }
            model.addAttribute("followings", followingUsernames);

            int followersCount = linkAutoServiceProxy.getUserFollowers(username).size();
            model.addAttribute("followersCount", followersCount);

            int followingCount = linkAutoServiceProxy.getUserFollowing(username).size();
            model.addAttribute("followingCount", followingCount);


            return "userProfile"; // Vista del perfil de usuario
        } else {
            return "redirect:/";
        }
    }
        @Autowired
    private ClientServiceProxy clientServiceProxy;

    // Endpoint para compartir una publicación
    @GetMapping("/posts/{postId}")
    public String sharePost(Model model, @PathVariable Long postId) {
        Post post = clientServiceProxy.sharePost(postId);
        model.addAttribute("post", post);
        Map<String, String> profilePictureByUsername = new HashMap<>();
        Map<Long, Comment> commentsByPostId = new HashMap<>();
        List<Comment> comments = linkAutoServiceProxy.getCommentsByPostId(post.id());
        for (Comment comment : comments) {
            String profilePicture = linkAutoServiceProxy.getUserByUsername(comment.username()).profilePicture();
            profilePictureByUsername.putIfAbsent(comment.username(), profilePicture);
            
            commentsByPostId.putIfAbsent(post.id(), comment);
        }
        model.addAttribute("profilePictureByUsername", profilePictureByUsername); // Agregar fotos de perfil al modelo
        model.addAttribute("commentsByPostId", commentsByPostId);
        return "post";
    }
    
    
}