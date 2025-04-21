package com.linkauto.restapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.linkauto.restapi.dto.CredencialesDTO;
import com.linkauto.restapi.dto.UserRegisterDTO;
import com.linkauto.restapi.dto.PostDTO;
import com.linkauto.restapi.dto.PostReturnerDTO;
import com.linkauto.restapi.dto.CommentDTO;
import com.linkauto.restapi.dto.CommentReturnerDTO;

/**
 * Prueba de integración para la funcionalidad de comentarios.
 * <p>
 * Flujo completo:
 *  <ol>
 *      <li>Registrar usuario</li>
 *      <li>Iniciar sesión y obtener token</li>
 *      <li>Crear un post de soporte</li>
 *      <li>Agregar un comentario a dicho post</li>
 *      <li>Obtener lista de comentarios del post y verificar contenido</li>
 *      <li>Eliminar el post (que también elimina comentarios en cascada)</li>
 *      <li>Eliminar el usuario</li>
 *  </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testUserCreatesAndVerifiesComment() {
        // 1. Registrar un nuevo usuario
        String username = "commentTestUser";
        UserRegisterDTO user = new UserRegisterDTO(
                username,
                "nombrePrueba",
                "avatar.jpg",
                "comment@test.com",
                List.of(), // coches
                0L,        
                "MALE",  
                "Madrid", 
                "pass123",
                "¡Hola a todos!" 
        );
        ResponseEntity<Void> regResponse = testRestTemplate.postForEntity(
                "/auth/register", user, Void.class);
        assertEquals(HttpStatus.CREATED, regResponse.getStatusCode());

        // 2. Login
        CredencialesDTO creds = new CredencialesDTO(username, "pass123");
        ResponseEntity<String> loginResponse = testRestTemplate.postForEntity(
                "/auth/login", creds, String.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = loginResponse.getBody();
        assertNotNull(token);

        // 3. post
        PostDTO postDTO = new PostDTO(
                "Post de prueba para comentarios",
                List.of("https://example.com/img.jpg")
        );
        ResponseEntity<PostReturnerDTO> postResp = testRestTemplate.postForEntity(
                "/api/posts?userToken=" + token,
                postDTO,
                PostReturnerDTO.class
        );
        assertEquals(HttpStatus.OK, postResp.getStatusCode());
        Long postId = postResp.getBody().getId();
        assertNotNull(postId);

        // 4. Agregar un comentario al post
        CommentDTO commentDTO = new CommentDTO("Este es un comentario de prueba");
        ResponseEntity<Void> commentResp = testRestTemplate.postForEntity(
                "/api/user/" + postId + "/comment?userToken=" + token,
                commentDTO,
                Void.class
        );
        assertEquals(HttpStatus.OK, commentResp.getStatusCode());

        // 5. Obtener lista de comentarios del post
        ResponseEntity<CommentReturnerDTO[]> getCommentsResp = testRestTemplate.getForEntity(
                "/api/post/" + postId + "/comments",
                CommentReturnerDTO[].class
        );
        assertEquals(HttpStatus.OK, getCommentsResp.getStatusCode());
        CommentReturnerDTO[] comments = getCommentsResp.getBody();
        assertNotNull(comments);
        assertTrue(comments.length > 0);
        boolean found = false;
        for (CommentReturnerDTO c : comments) {
            if ("Este es un comentario de prueba".equals(c.getText()) && c.getPost_id().equals(postId)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "El comentario creado debe aparecer en la lista");

        // 6. Limpiar: eliminar post / comentarios
        ResponseEntity<Void> deletePostResp = testRestTemplate.exchange(
                "/api/posts/" + postId + "?userToken=" + token,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertEquals(HttpStatus.OK, deletePostResp.getStatusCode());

        // 7. Eliminar usuario
        ResponseEntity<Void> deleteUserResp = testRestTemplate.exchange(
                "/api/user/" + username + "?userToken=" + token,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertEquals(HttpStatus.OK, deleteUserResp.getStatusCode());
    }
}
