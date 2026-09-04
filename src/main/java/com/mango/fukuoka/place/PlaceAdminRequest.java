package com.mango.fukuoka.place;

import java.math.BigDecimal;

public record PlaceAdminRequest(
        String name,
        String nameJa,
        String slug,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String thumbnailImage
) {
}