package com.mango.fukuoka.place;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceAdminResponse(
        Long id,
        Long cityId,
        String citySlug,
        String name,
        String nameJa,
        String slug,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String thumbnailImage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}