package com.linkauto.client.data;

import java.util.List;
import java.util.Set;

public record Post(
    long id,
    String username,
    String message,
    long creationDate,
    List<String> images,
    List<Long> comment_ids,
    Set<String> likes
) {} 
