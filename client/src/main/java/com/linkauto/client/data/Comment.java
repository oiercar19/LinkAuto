package com.linkauto.client.data;

public record Comment (
    long id,
    String text,
    String username,
    long post_id,
    long creationDate
) {}