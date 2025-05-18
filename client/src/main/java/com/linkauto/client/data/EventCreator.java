package com.linkauto.client.data;

import java.util.List;

public record EventCreator(
    String title,
    String description,
    String location,
    Long startDate,
    Long endDate,
    List<String> images
) {
} 
