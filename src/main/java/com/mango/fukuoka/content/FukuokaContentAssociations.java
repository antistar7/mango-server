package com.mango.fukuoka.content;

import com.mango.fukuoka.content.expression.JapaneseExpressionRepository;
import com.mango.fukuoka.content.expression.JapaneseExpressionResponse;
import com.mango.fukuoka.content.image.FukuokaContentImage;
import com.mango.fukuoka.content.image.FukuokaContentImageRepository;
import com.mango.fukuoka.content.image.FukuokaContentImageResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 콘텐츠 목록에 딸린 이미지/표현을 콘텐츠 수와 무관하게 각각 한 번의 조회로 읽어온다.
 */
@Component
public class FukuokaContentAssociations {

    private final FukuokaContentImageRepository imageRepository;
    private final JapaneseExpressionRepository expressionRepository;

    public FukuokaContentAssociations(
            FukuokaContentImageRepository imageRepository,
            JapaneseExpressionRepository expressionRepository
    ) {
        this.imageRepository = imageRepository;
        this.expressionRepository = expressionRepository;
    }

    public Map<Long, List<FukuokaContentImageResponse>> imagesByContentId(
            Collection<FukuokaContent> contents
    ) {
        List<Long> contentIds = contentIds(contents);

        if (contentIds.isEmpty()) {
            return Map.of();
        }

        return imageRepository
                .findByContentIdInOrderByContentIdAscSortOrderAsc(contentIds)
                .stream()
                .collect(Collectors.groupingBy(
                        image -> image.getContent().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                FukuokaContentAssociations::toImageResponse,
                                Collectors.toList()
                        )
                ));
    }

    public Map<Long, List<JapaneseExpressionResponse>> expressionsByContentId(
            Collection<FukuokaContent> contents
    ) {
        List<Long> contentIds = contentIds(contents);

        if (contentIds.isEmpty()) {
            return Map.of();
        }

        return expressionRepository
                .findByContentIdInOrderByContentIdAscIdAsc(contentIds)
                .stream()
                .collect(Collectors.groupingBy(
                        expression -> expression.getContent().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                JapaneseExpressionResponse::from,
                                Collectors.toList()
                        )
                ));
    }

    public List<FukuokaContentImageResponse> imagesOf(Long contentId) {
        return imageRepository
                .findByContentIdOrderBySortOrderAsc(contentId)
                .stream()
                .map(FukuokaContentAssociations::toImageResponse)
                .toList();
    }

    public List<JapaneseExpressionResponse> expressionsOf(Long contentId) {
        return expressionRepository
                .findByContentIdOrderByIdAsc(contentId)
                .stream()
                .map(JapaneseExpressionResponse::from)
                .toList();
    }

    private static List<Long> contentIds(Collection<FukuokaContent> contents) {
        return contents.stream()
                .map(FukuokaContent::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static FukuokaContentImageResponse toImageResponse(
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
