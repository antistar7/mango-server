package com.mango.fukuoka.content.image;

public record FukuokaContentImageResponse(
        Long id,
        String imageUrl,
        String imageType,
        Integer sortOrder,
        String caption
) {
    public static FukuokaContentImageResponse from(
            FukuokaContentImage image
    ) {
        return new FukuokaContentImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getImageType(),
                image.getSortOrder(),
                image.getCaption()
        );
    }
}
