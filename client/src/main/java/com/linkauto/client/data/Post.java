package com.linkauto.client.data;

import java.util.List;

public record Post(
    long id,
    String username,
    String message,
    long creationDate,
    List<String> images
) {} 
