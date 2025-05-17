package com.linkauto.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linkauto.client.data.Comment;
import com.linkauto.client.data.CommentCreator;
import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Post;
import com.linkauto.client.data.PostCreator;
import com.linkauto.client.data.User;
import com.linkauto.client.service.ClientServiceProxy;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ClientController {

    @Autowired
    public ClientServiceProxy linkAutoServiceProxy;

    public String token; // Stores the session token
    public String username; // Stores the username

    public void addAttributes(Model model, HttpServletRequest request) {
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
        model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, RedirectAttributes redirectAttributes, Model model) {
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
            Set<String> verifiedUsers = new HashSet<>();
            for (Post post : posts) {
                String profilePicture = linkAutoServiceProxy.getUserByUsername(post.username()).profilePicture();
                profilePictureByUsername.putIfAbsent(post.username(), profilePicture);

                if (verifiedUsers.contains(post.username())) {
                    continue;
                }
                Boolean isUserVerified = linkAutoServiceProxy.isUserVerified(post.username());
                if (isUserVerified) {
                    verifiedUsers.add(post.username());
                }
            }
            model.addAttribute("verifiedUsers", verifiedUsers); // Agregar usuarios verificados al modelo

            }

            model.addAttribute("profilePictureByUsername", profilePictureByUsername); // Agregar fotos de perfil al modelo

            model.addAttribute("posts", posts); // Agregar publicaciones al modelo
            model.addAttribute("username", username); // Agregar nombre de usuario al modelo

            User user = linkAutoServiceProxy.getUserProfile(token);
            String profilePicture = user.profilePicture();
            model.addAttribute("profilePicture", profilePicture); // Agregar foto de perfil al modelo
            model.addAttribute("role", user.role()); // Agregar rol al modelo

            List<User> followings = linkAutoServiceProxy.getUserFollowing(username);
            List<String> followingUsernames = new ArrayList<>();
            for (User following : followings) {
                followingUsernames.add(following.username());
            }
            model.addAttribute("followings", followingUsernames); // Agregar seguidores al modelo

            Map<Long, List<Comment>> commentsByPostId = new HashMap<>();
            for (Post post : posts) {
                List<Comment> comments = linkAutoServiceProxy.getCommentsByPostId(post.id());

                for (Comment comment : comments) {
                    commentsByPostId.putIfAbsent(post.id(), new ArrayList<>());
                    commentsByPostId.get(post.id()).add(comment);
                }
            }
            model.addAttribute("commentsByPostId", commentsByPostId); // Agregar comentarios al modelo

            model.addAttribute("isVerified", user.isVerified()); // Agregar verificación al modelo

            List<Post> savedPosts = new ArrayList<>(linkAutoServiceProxy.getUserSavedPosts(username));
            model.addAttribute("savedPosts", savedPosts); // Agregar publicaciones guardadas al modelo


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

            model.addAttribute("role", user.role()); // Agregar rol al modelo

            User currentUser = linkAutoServiceProxy.getUserProfile(token);
            model.addAttribute("currentUser", currentUser); // Agregar usuario al modelo

            List<Post> userPosts = new ArrayList<>(linkAutoServiceProxy.getUserPosts(username));
            model.addAttribute("userPosts", userPosts); // Agregar publicaciones al modelo

            Map<String, String> profilePictureByUsername = new HashMap<>();
            Map<Long, List<Comment>> commentsByPostId = new HashMap<>();
            for (Post post : userPosts) {
                List<Comment> comments = linkAutoServiceProxy.getCommentsByPostId(post.id());

                for (Comment comment : comments) {
                    String profilePicture = linkAutoServiceProxy.getUserByUsername(comment.username()).profilePicture();
                    profilePictureByUsername.putIfAbsent(comment.username(), profilePicture);

                    commentsByPostId.putIfAbsent(post.id(), new ArrayList<>());
                    commentsByPostId.get(post.id()).add(comment);
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

    @GetMapping("/search")
    public String searchUsers(@RequestParam(required = false) String username, Model model, RedirectAttributes redirectAttributes) {
        if (token == null) {
            return "redirect:/";
        }

        try {
            // Si el término de búsqueda es null o vacío, redirigir al feed
            if (username == null || username.trim().isEmpty()) {
                return "redirect:/feed";
            }

            // Obtener todos los usuarios
            List<User> allUsers = linkAutoServiceProxy.getAllUsers();

            // Filtrar usuarios que coincidan con el término de búsqueda (ignorando mayúsculas y minúsculas)
            List<User> matchingUsers = allUsers.stream()

                .filter(user -> user.username().toLowerCase().contains(username.toLowerCase()))
                .collect(Collectors.toList());

            Set <String> verifiedUsers = new HashSet<>();
            for (User user : matchingUsers) {
                Boolean isUserVerified = linkAutoServiceProxy.isUserVerified(user.username());
                if (isUserVerified) {
                    verifiedUsers.add(user.username());
                }
            }
            model.addAttribute("verifiedUsers", verifiedUsers);
                    .filter(user -> user.username().toLowerCase().contains(username.toLowerCase()))
                    .collect(Collectors.toList());

            model.addAttribute("searchTerm", username);
            model.addAttribute("users", matchingUsers);

            // Añadir datos del usuario actual
            User currentUser = linkAutoServiceProxy.getUserProfile(token);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("username", this.username);
            model.addAttribute("profilePicture", currentUser.profilePicture());
            model.addAttribute("role", currentUser.role());

            // Obtener usuarios que sigue el usuario actual
            List<User> followings = linkAutoServiceProxy.getUserFollowing(this.username);
            List<String> followingUsernames = followings.stream()
                    .map(User::username)
                    .collect(Collectors.toList());
            model.addAttribute("followings", followingUsernames);

            return "searchResults"; // Nueva plantilla para mostrar resultados
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al buscar usuarios: " + e.getMessage());
            return "redirect:/feed";
        }
    }

    @PostMapping("/user/{postId}/like")
    public String likePost(@PathVariable Long postId, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.likePost(token, postId);
            redirectAttributes.addFlashAttribute("success", "Publicación " + postId + " le gusta");
            return "redirect:/feed"; // Redirigir a la página de inicio después de dar me gusta a la publicación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al dar me gusta a la publicación: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/user/{postId}/unlike")
    public String unlikePost(@PathVariable Long postId, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.unlikePost(token, postId);
            redirectAttributes.addFlashAttribute("success", "Publicación " + postId + " ya no le gusta");
            return "redirect:/feed"; // Redirigir a la página de inicio después de quitar el me gusta a la publicación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al quitar el me gusta a la publicación: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/user/{postId}/comment")
    public String commentPost(@PathVariable Long postId, @ModelAttribute CommentCreator comment, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.commentPost(token, postId, comment);
            redirectAttributes.addFlashAttribute("success", "Comentario agregado con éxito");
            return "redirect:/feed"; // Redirigir a la página de inicio después de comentar en la publicación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar el comentario: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/user/{postId}/commentInProfile")
    public String commentPostInProfile(@PathVariable Long postId, @ModelAttribute CommentCreator comment, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.commentPost(token, postId, comment);
            redirectAttributes.addFlashAttribute("success", "Comentario agregado con éxito");
            return "redirect:/user/" + linkAutoServiceProxy.getPostById(postId).username(); // Redirigir a la página de inicio después de comentar en la publicación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar el comentario: " + e.getMessage());
            return "redirect:/user/" + linkAutoServiceProxy.getPostById(postId).username(); // Redirigir a la página de inicio en caso de error
        }
    }

    // Endpoint para compartir una publicación
    @GetMapping("/posts/{postId}")
    public String sharePost(Model model, @PathVariable Long postId) {
        Post post = linkAutoServiceProxy.sharePost(postId);
        model.addAttribute("post", post);
        Map<String, String> profilePictureByUsername = new HashMap<>();
        Map<Long, Comment> commentsByPostId = new HashMap<>();
        List<Comment> comments = linkAutoServiceProxy.getCommentsByPostId(post.id());
        for (Comment comment : comments) {
            String profilePicture = linkAutoServiceProxy.getUserByUsername(comment.username()).profilePicture();
            profilePictureByUsername.putIfAbsent(comment.username(), profilePicture);

            commentsByPostId.putIfAbsent(post.id(), comment);
        }
        String profilePicture = linkAutoServiceProxy.getUserByUsername(post.username()).profilePicture();
        model.addAttribute("profilePicture", profilePicture); // Agregar foto de perfil al modelo
        model.addAttribute("profilePictureByUsername", profilePictureByUsername); // Agregar fotos de perfil al modelo
        model.addAttribute("commentsByPostId", commentsByPostId);
        return "post";
    }
    
  //Endpoint para el panel de administrador
  @GetMapping("/adminPanel")
  public String adminPanel(Model model, RedirectAttributes redirectAttributes) {
      if (token == null) {
          // Si no hay token, redirigir al inicio de sesión
          redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder al panel de administrador.");
          return "redirect:/";
      }

      String profilePicture = linkAutoServiceProxy.getUserByUsername(username).profilePicture();
      model.addAttribute("profilePicture", profilePicture); // Agregar fotos de perfil al modelo
            
      model.addAttribute("username", username); // Agregar nombre de usuario al modelo
      

      // Obtener el perfil del usuario logueado
      User user = linkAutoServiceProxy.getUserProfile(token);
  
      // Verificar si el usuario tiene el rol de ADMIN
      if (!user.role().equals("ADMIN")) {
          redirectAttributes.addFlashAttribute("error", "No tienes permisos para acceder al panel de administrador.");
          return "redirect:/feed"; // Redirigir al feed si no es administrador
      }

      List<User> users = linkAutoServiceProxy.getAllUsers();
      model.addAttribute("users", users);
  
      return "adminPanel"; // Vista del panel de administrador
  }

  @PostMapping("/admin/deleteUser")
  public String deleteUser(@RequestParam("username") String usernameToDelete, RedirectAttributes redirectAttributes) {
      try {
          System.out.println("Attempting to delete user: " + usernameToDelete); // Debug log
          linkAutoServiceProxy.deleteUser(token, usernameToDelete);
          redirectAttributes.addFlashAttribute("success", "Usuario " + usernameToDelete + " eliminado con éxito.");
      } catch (Exception e) {
          System.err.println("Error deleting user: " + e.getMessage()); // Debug log
          redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
      }
      return "redirect:/adminPanel"; // Redirect back to the admin panel
  }
  
  @PostMapping("/admin/promoteToAdmin")
  public String promoteToAdmin(@RequestParam("username") String usernameToPromote, RedirectAttributes redirectAttributes) {
      try {
          linkAutoServiceProxy.promoteToAdmin(token, usernameToPromote);
          redirectAttributes.addFlashAttribute("success", "Usuario " + usernameToPromote + " promovido a administrador con éxito.");
      } catch (Exception e) {
          redirectAttributes.addFlashAttribute("error", "Error al promover al usuario: " + e.getMessage());
      }
      return "redirect:/adminPanel"; // Redirect back to the admin panel
  }
  
  
  @PostMapping("/admin/demoteToUser")
  public String demoteToUser(@RequestParam("username") String usernameToDemote, RedirectAttributes redirectAttributes) {
      try {
          linkAutoServiceProxy.demoteToUser(token, usernameToDemote);
          redirectAttributes.addFlashAttribute("success", "Administrador " + usernameToDemote + " degradado con éxito.");
      } catch (Exception e) {
          redirectAttributes.addFlashAttribute("error", "Error al degradar al administrador: " + e.getMessage());
      }
      return "redirect:/adminPanel"; // Redirect back to the admin panel
  }

  @PostMapping("/user/{username}/verify")
    public String verifyUser(@PathVariable String username, RedirectAttributes redirectAttributes, String redirect) {
        try {
            linkAutoServiceProxy.verifyUser(token, username);
            redirectAttributes.addFlashAttribute("success", "Usuario " + username + " verificado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al verificar al usuario: " + e.getMessage());
        }
        return "redirect:" + redirect; // Redirect back to the admin panel
    }
}

    //Endpoint para el panel de administrador
    @GetMapping("/adminPanel")
    public String adminPanel(Model model, RedirectAttributes redirectAttributes) {
        if (token == null) {
            // Si no hay token, redirigir al inicio de sesión
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder al panel de administrador.");
            return "redirect:/";
        }

        String profilePicture = linkAutoServiceProxy.getUserByUsername(username).profilePicture();
        model.addAttribute("profilePicture", profilePicture); // Agregar fotos de perfil al modelo

        model.addAttribute("username", username); // Agregar nombre de usuario al modelo

        // Obtener el perfil del usuario logueado
        User user = linkAutoServiceProxy.getUserProfile(token);

        // Verificar si el usuario tiene el rol de ADMIN
        if (!user.role().equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para acceder al panel de administrador.");
            return "redirect:/feed"; // Redirigir al feed si no es administrador
        }

        List<User> users = linkAutoServiceProxy.getAllUsers();
        model.addAttribute("users", users);

        return "adminPanel"; // Vista del panel de administrador
    }

    @PostMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam("username") String usernameToDelete, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Attempting to delete user: " + usernameToDelete); // Debug log
            linkAutoServiceProxy.deleteUser(token, usernameToDelete);
            redirectAttributes.addFlashAttribute("success", "Usuario " + usernameToDelete + " eliminado con éxito.");
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage()); // Debug log
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/adminPanel"; // Redirect back to the admin panel
    }

    @PostMapping("/admin/promoteToAdmin")
    public String promoteToAdmin(@RequestParam("username") String usernameToPromote, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.promoteToAdmin(token, usernameToPromote);
            redirectAttributes.addFlashAttribute("success", "Usuario " + usernameToPromote + " promovido a administrador con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al promover al usuario: " + e.getMessage());
        }
        return "redirect:/adminPanel"; // Redirect back to the admin panel
    }

    @PostMapping("/admin/demoteToUser")
    public String demoteToUser(@RequestParam("username") String usernameToDemote, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.demoteToUser(token, usernameToDemote);
            redirectAttributes.addFlashAttribute("success", "Administrador " + usernameToDemote + " degradado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al degradar al administrador: " + e.getMessage());
        }
        return "redirect:/adminPanel"; // Redirect back to the admin panel
    }

    @PostMapping("/user/{postId}/savePost")
    public String savePost(@PathVariable Long postId, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.savePost(token, postId);
            redirectAttributes.addFlashAttribute("success", "Publicación " + postId + " guardada con éxito");
            return "redirect:/feed"; // Redirigir a la página de inicio después de guardar la publicación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la publicación: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @PostMapping("/user/{postId}/unSavePost")
    public String unsavePost(@PathVariable Long postId, RedirectAttributes redirectAttributes) {
        try {
            linkAutoServiceProxy.unsavePost(token, postId);
            redirectAttributes.addFlashAttribute("success", "Publicación " + postId + " eliminada de guardados");
            return "redirect:/feed"; // Redirigir a la página de inicio después de quitar la publicación de guardados
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al quitar la publicación de guardados: " + e.getMessage());
            return "redirect:/feed"; // Redirigir a la página de inicio en caso de error
        }
    }

    @GetMapping("/savedPosts")
    public String savedPosts(Model model) {
        if (token != null) {
            List<Post> posts = new ArrayList<>(linkAutoServiceProxy.getUserSavedPosts(username));
            Map<String, String> profilePictureByUsername = new HashMap<>();
            for (Post post : posts) {
                String profilePicture = linkAutoServiceProxy.getUserByUsername(post.username()).profilePicture();
                profilePictureByUsername.putIfAbsent(post.username(), profilePicture);

            }

            model.addAttribute("profilePictureByUsername", profilePictureByUsername); // Agregar fotos de perfil al modelo

            model.addAttribute("posts", posts); // Agregar publicaciones al modelo
            model.addAttribute("username", username); // Agregar nombre de usuario al modelo

            User user = linkAutoServiceProxy.getUserProfile(token);
            String profilePicture = user.profilePicture();
            model.addAttribute("profilePicture", profilePicture); // Agregar foto de perfil al modelo
            model.addAttribute("role", user.role()); // Agregar rol al modelo

            List<User> followings = linkAutoServiceProxy.getUserFollowing(username);
            List<String> followingUsernames = new ArrayList<>();
            for (User following : followings) {
                followingUsernames.add(following.username());
            }
            model.addAttribute("followings", followingUsernames); // Agregar seguidores al modelo

            Map<Long, List<Comment>> commentsByPostId = new HashMap<>();
            for (Post post : posts) {
                List<Comment> comments = linkAutoServiceProxy.getCommentsByPostId(post.id());

                for (Comment comment : comments) {
                    commentsByPostId.putIfAbsent(post.id(), new ArrayList<>());
                    commentsByPostId.get(post.id()).add(comment);
                }
            }
            model.addAttribute("commentsByPostId", commentsByPostId); // Agregar comentarios al modelo

            return "savedPost"; // Vista para usuarios autenticados
        } else {
            // Token inválido o no proporcionado, redirigir al inicio de sesión
            return "redirect:/";
        }
    }
}
