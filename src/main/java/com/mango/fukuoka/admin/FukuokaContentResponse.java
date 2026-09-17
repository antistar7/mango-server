package com.mango.fukuoka.admin;

import com.mango.fukuoka.content.expression.JapaneseExpressionResponse;
import com.mango.fukuoka.content.image.FukuokaContentImageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FukuokaContentResponse(
        Long id,
        String title,
        String subtitle,
        String summary,
        String body,
        Long placeId,
        String placeName,
        String placeNameJa,
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
        List<JapaneseExpressionResponse> expressions,
        List<FukuokaContentImageResponse> images
) {
}
