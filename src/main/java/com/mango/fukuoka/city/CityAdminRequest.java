package com.mango.fukuoka.city;

public record CityAdminRequest(
        String slug,
        String name,
        String nameJa,
        String description,
        String heroImage,
        Boolean active,
        Integer sortOrder
) {
}
