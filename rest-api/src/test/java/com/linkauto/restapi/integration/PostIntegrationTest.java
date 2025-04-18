package com.linkauto.restapi.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.linkauto.restapi.dto.CredencialesDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.dto.UserRegisterDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testUserCreatesAndDeletesPost() {
        // Create a new user
        List<String> cars = new ArrayList<>();
        cars.add("Audi R8");
        cars.add("Nissan Skyline");
        UserRegisterDTO user = new UserRegisterDTO("integTestUser", "userPruebas", "pp.jpg", "user@example.com", cars, 999L, "MALE", "Bilbao", "user123", "hola a todos"); 
        ResponseEntity<Void> response = testRestTemplate.postForEntity("/auth/register", user, Void.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Login the user
        CredencialesDTO credentials = new CredencialesDTO("integTestUser", "user123");
        ResponseEntity<String> response2 = testRestTemplate.postForEntity("/auth/login", credentials, String.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        String token = response2.getBody();
        assertNotNull(token);

        // Create a post
        PostDTO postDTO = new PostDTO("Post content", Arrays.asList("https://example.com/image.jpg", "https://example.com/image2.jpg"));
        ResponseEntity<PostReturnerDTO> response3 = testRestTemplate.postForEntity(
            "/api/posts?userToken=" + token, 
            postDTO, 
            PostReturnerDTO.class
        );
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        Long postId = response3.getBody().getId();
        assertNotNull(postId);
        
        // Verify the post was created
        ResponseEntity<PostReturnerDTO> response4 = testRestTemplate.getForEntity(
            "/api/posts/" + postId + "?userToken=" + token,
            PostReturnerDTO.class
        );
        assertEquals(HttpStatus.OK, response4.getStatusCode());

        // Delete the post
        ResponseEntity<Void> response5 = testRestTemplate.exchange(
            "/api/posts/" + postId + "?userToken=" + token,
            HttpMethod.DELETE,
            null,
            Void.class
        );
        assertEquals(HttpStatus.OK, response5.getStatusCode());

        // Verify the post was deleted
        ResponseEntity<PostReturnerDTO> response6 = testRestTemplate.getForEntity(
            "/api/posts/" + postId + "?userToken=" + token, 
            PostReturnerDTO.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response6.getStatusCode());

        // Delete the user
        System.out.println("User token: " + token);
        ResponseEntity<Void> response7 = testRestTemplate.exchange(
            "/api/user/integTestUser?userToken=" + token,
            HttpMethod.DELETE,
            null,
            Void.class
        );
        assertEquals(HttpStatus.OK, response7.getStatusCode());
    }
}