package com.example.restapi.client.templates.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.client.templates.model.Usuario;
import com.example.restapi.model.CredencialesDTO;

@Service
public class ClientService {

    private final RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public ClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String login(String email, String password) {
        String url = apiBaseUrl + "/auth/login";
        try {
            return restTemplate.postForObject(url, new CredencialesDTO(email, password), String.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Login failed: Invalid credentials.");
                default -> throw new RuntimeException("Login failed: " + e.getStatusText());
            }
        }
    }

    public void logout(String token) {
        String url = String.format("%s/auth/logout?userToken=%s", apiBaseUrl, token);
        try {
            restTemplate.postForObject(url, token, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Logout failed: Invalid token.");
                default -> throw new RuntimeException("Logout failed: " + e.getStatusText());
            }
        }
    }

    public void createUsuario(Usuario usuario) {
        String url = apiBaseUrl + "/usuarios";
        try {
            restTemplate.postForObject(url, usuario, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 400 -> throw new RuntimeException("User creation failed");
                default -> throw new RuntimeException("User creation failed: " + e.getStatusText());
            }
        }
    }

    public Usuario getUsuarioByToken(String token) {
        String url = String.format("%s/usuarios?userToken=%s", apiBaseUrl, token);
        try {
            return restTemplate.getForObject(url, Usuario.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                default -> throw new RuntimeException("Failed to retrieve user: " + e.getStatusText());
            }
        }
    }

    public void updateUsuario(int id, Usuario usuario) {
        String url = String.format("%s/usuarios/%d", apiBaseUrl, id);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(usuario, headers), Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token");
                default -> throw new RuntimeException("Failed to update user: " + e.getStatusText());
            }
        }
    }
}