package com.linkauto.client.data;

import java.util.List;

public record UpdateUser(
    String username,
    String name,
    String profilePicture,
    String email,
    List<String> cars,
    long birthDate,
    String gender,
    String location,
    String password,
    String description
) {}


