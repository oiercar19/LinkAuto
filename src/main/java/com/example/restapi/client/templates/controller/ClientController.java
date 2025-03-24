package com.example.restapi.client.templates.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.restapi.client.templates.service.ClientService;
import com.example.restapi.client.templates.model.Template;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    private String token; // Stores the session token

    // Add current URL and token to all views
    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
        model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
        model.addAttribute("token", token); // Makes token available in all templates
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegister(@RequestParam(value = "redirectUrl", required = false) String redirectUrl, Model model) {
        model.addAttribute("redirectUrl", redirectUrl);
        return "register";
    }

    @PostMapping("/register")
    public String performRegister(@RequestBody Template template, RedirectAttributes redirectAttributes) {
        try {
            clientService.createTemplate(template);
            redirectAttributes.addFlashAttribute("message", "User registered successfully");
            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error during registration");
            return "errorPage";
        }
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "redirectUrl", required = false) String redirectUrl, Model model) {
        model.addAttribute("redirectUrl", redirectUrl);
        return "login";
    }

    @PostMapping("/login")
    public String performLogin(@RequestParam("email") String email, @RequestParam("password") String password,
                               @RequestParam(value = "redirectUrl", required = false) String redirectUrl, Model model) {
        try {
            token = clientService.login(email, password);
            return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/");
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String performLogout(@RequestParam(value = "redirectUrl", defaultValue = "/") String redirectUrl, Model model) {
        try {
            clientService.logout(token);
            token = null;
            model.addAttribute("successMessage", "Logout successful.");
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Logout failed: " + e.getMessage());
            return "errorPage";
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/editProfile")
    public String showEditProfile(Model model) {
        if (!isLogged(token)) return "redirect:/login?redirectUrl=/editProfile";
        Template template = clientService.getTemplateByToken(token);
        model.addAttribute("template", template);
        return "editProfile";
    }

    @PostMapping("/editProfile")
    public String performEditProfile(@RequestBody Template templateDetails, RedirectAttributes redirectAttributes) {
        try {
            clientService.updateTemplate(templateDetails.getId(), templateDetails);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            return "redirect:/editProfile";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error updating the profile");
            return "errorPage";
        }
    }

    public boolean isLogged(String token) {
        return token != null;
    }
}