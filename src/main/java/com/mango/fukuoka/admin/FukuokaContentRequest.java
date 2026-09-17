package com.mango.fukuoka.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FukuokaContentRequest(
        String title,
        String subtitle,
        String summary,
        String body,
        Long placeId,
        BigDecimal storeLatitude,
        BigDecimal storeLongitude,
        String storeAddress,
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
