package com.linkauto.client.data;

import java.util.List;

public record PostCreator(
    String message,
    List<String> images
) {} 
