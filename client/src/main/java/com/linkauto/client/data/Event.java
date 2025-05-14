package com.linkauto.client.data;

import java.util.List;
import java.util.Set;

public record Event(
    long id,
    String username,
    String title,
    String description,
    String location,
    long startDate,
    long endDate,
    List<String> images,
    Set<String> participants,
    List<Long> comment_ids
) {}
