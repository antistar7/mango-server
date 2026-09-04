package com.mango.fukuoka.admin;

import java.time.LocalDateTime;
import java.util.List;

public record FukuokaContentRequest(
        String title,
        String subtitle,
        String summary,
        String body,
        Long placeId,
        String thumbnailImage,
        String heroImage,
        String status,
        LocalDateTime publishedAt,
        Boolean mapVisible,
        Boolean isMangoPick,
        Integer mangoPickOrder,
        List<Long> categoryIds,
        List<FukuokaExpressionRequest> expressions,
        List<FukuokaImageRequest> images
) {
}
