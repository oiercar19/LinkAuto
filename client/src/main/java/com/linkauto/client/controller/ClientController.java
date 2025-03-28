package com.linkauto.client.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linkauto.client.data.Credentials;
import com.linkauto.client.data.Post;
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
            model.addAttribute("posts", posts); // Agregar publicaciones al modelo
            model.addAttribute("username", username); // Agregar nombre de usuario al modelo
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
    
}