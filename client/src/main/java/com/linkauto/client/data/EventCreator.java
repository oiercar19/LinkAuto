package com.linkauto.client.data;

import java.util.List;

public record EventCreator(
    String title,
    String description,
    String location,
    String startDate,
    String endDate,
    List<String> images
) {
} 
