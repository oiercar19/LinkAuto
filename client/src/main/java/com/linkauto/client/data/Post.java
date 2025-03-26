package com.linkauto.client.data;

import java.util.List;

public record Post(
    long id,
    User usuario,
    String mensaje,
    long fechaCreacion,
    List<String> imagenes
) {} 
