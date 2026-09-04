package com.mango.fukuoka.city;

import java.time.LocalDateTime;

public record CityAdminResponse(
        Long id,
        String slug,
        String name,
        String nameJa,
        String description,
        Boolean active,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
