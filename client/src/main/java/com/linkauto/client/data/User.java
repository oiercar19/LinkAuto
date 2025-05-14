package com.linkauto.client.data;

import java.util.List;
import java.util.Set;

public record User(
    String username,
    String role,
    String name,
    String profilePicture,
    String email,
    List<String> cars,
    long birthDate,
    String gender,
    String location,
    String password,
    String description,
    Set<User> reporters
) {}


