package com.linkauto.restapi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ShareReturnerDTOTest {

    @Test
    public void testNoArgsConstructor() {
        ShareReturnerDTO dto = new ShareReturnerDTO();
        assertNull(dto.getlinkString());  // Espera null porque no se ha asignado valor aún
    }

    @Test
    public void testSettersAndGetters() {
        ShareReturnerDTO dto = new ShareReturnerDTO();

        String expectedLink = "https://linkauto.com/share/abc123";
        dto.setMessage(expectedLink);  // Establece el link con el setter

        assertEquals(expectedLink, dto.getlinkString());  // Comprueba que el getter devuelve lo que se estableció
    }
}

