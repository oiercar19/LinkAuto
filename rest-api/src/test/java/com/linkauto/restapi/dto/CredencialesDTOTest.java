package com.linkauto.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CredencialesDTOTest {

    @Test
    public void testNoArgsConstructor() {
        CredencialesDTO credencialesDTO = new CredencialesDTO();
        assertNull(credencialesDTO.getUsuario());
        assertNull(credencialesDTO.getContrasena());
    }

    @Test
    public void testAllArgsConstructor() {
        String usuario = "testUser";
        String contrasena = "testPassword";
        CredencialesDTO credencialesDTO = new CredencialesDTO(usuario, contrasena);

        assertEquals(usuario, credencialesDTO.getUsuario());
        assertEquals(contrasena, credencialesDTO.getContrasena());
    }

    @Test
    public void testSettersAndGetters() {
        CredencialesDTO credencialesDTO = new CredencialesDTO();

        String usuario = "nuevoUsuario";
        String contrasena = "nuevaContrasena";

        credencialesDTO.setUsuario(usuario);
        credencialesDTO.setContrasena(contrasena);

        assertEquals(usuario, credencialesDTO.getUsuario());
        assertEquals(contrasena, credencialesDTO.getContrasena());
    }

    @Test
    public void testToString() {
        String usuario = "usuarioTest";
        String contrasena = "contrasenaTest";
        CredencialesDTO credencialesDTO = new CredencialesDTO(usuario, contrasena);

        String toStringResult = credencialesDTO.toString();

        assertTrue(toStringResult.contains("usuario='usuarioTest'"));
        assertTrue(toStringResult.contains("contrasena='contrasenaTest'"));
    }
}
