package com.mango.fukuoka.city;

public record CityAdminRequest(
        String slug,
        String name,
        String nameJa,
        String description,
        Boolean active,
        Integer sortOrder
) {
}
