package com.mango.fukuoka.admin;

public record FukuokaImageRequest(
        String imageUrl,
        String imageType,
        Integer sortOrder,
        String caption
) {
}
