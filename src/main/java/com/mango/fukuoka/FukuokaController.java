package com.mango.fukuoka;

import com.mango.fukuoka.category.FukuokaCategoryRepository;
import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentAssociations;
import com.mango.fukuoka.content.FukuokaContentRepository;
import com.mango.fukuoka.content.expression.JapaneseExpressionResponse;
import com.mango.fukuoka.content.image.FukuokaContentImageResponse;
import com.mango.fukuoka.place.FukuokaPlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fukuoka")
public class FukuokaController {

    private final FukuokaPlaceRepository placeRepository;
    private final FukuokaCategoryRepository categoryRepository;
    private final FukuokaContentRepository contentRepository;
    private final FukuokaContentAssociations contentAssociations;

    public FukuokaController(
            FukuokaPlaceRepository placeRepository,
            FukuokaCategoryRepository categoryRepository,
            FukuokaContentRepository contentRepository,
            FukuokaContentAssociations contentAssociations
    ) {
        this.placeRepository = placeRepository;
        this.categoryRepository = categoryRepository;
        this.contentRepository = contentRepository;
        this.contentAssociations = contentAssociations;
    }

    @GetMapping("/contents")
    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<FukuokaContentResponse> contents() {

        List<FukuokaContent> contents = contentRepository
                .findByStatusOrderByPublishedAtDesc("PUBLISHED");

        Map<Long, List<FukuokaContentImageResponse>> images =
                contentAssociations.imagesByContentId(contents);

        Map<Long, List<JapaneseExpressionResponse>> expressions =
                contentAssociations.expressionsByContentId(contents);

        return contents.stream()
                .map(content -> toResponse(
                        content,
                        images.getOrDefault(content.getId(), List.of()),
                        expressions.getOrDefault(content.getId(), List.of())
                ))
                .toList();
    }

    @GetMapping("/contents/{id}")
    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public FukuokaContentResponse content(
            @PathVariable Long id
    ) {

        FukuokaContent content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "콘텐츠를 찾을 수 없습니다: " + id
                        )
                );

        if (!"PUBLISHED".equals(content.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "공개되지 않은 콘텐츠입니다: " + id
            );
        }

        return toResponse(
                content,
                contentAssociations.imagesOf(content.getId()),
                contentAssociations.expressionsOf(content.getId())
        );
    }

    @GetMapping("/places")
    public List<FukuokaPlaceResponse> places() {

        return placeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(place -> new FukuokaPlaceResponse(
                        place.getId(),
                        place.getName(),
                        place.getNameJa(),
                        place.getSlug()
                ))
                .toList();
    }

    @GetMapping("/categories")
    public List<FukuokaCategoryResponse> categories() {

        return categoryRepository
                .findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(category -> new FukuokaCategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getNameJa(),
                        category.getSlug(),
                        category.getIcon()
                ))
                .toList();
    }

    private FukuokaContentResponse toResponse(
            FukuokaContent content,
            List<FukuokaContentImageResponse> images,
            List<JapaneseExpressionResponse> expressions
    ) {
        return new FukuokaContentResponse(
                content.getId(),
                content.getTitle(),
                content.getSubtitle(),
                content.getSummary(),
                content.getBody(),

                content.getPlace() != null
                        ? content.getPlace().getId()
                        : null,

                content.getPlace() != null
                        ? content.getPlace().getName()
                        : null,

                content.getPlace() != null
                        ? content.getPlace().getNameJa()
                        : null,

                content.getThumbnailImage(),
                content.getHeroImage(),
                content.getStatus(),
                content.getPublishedAt(),
                content.getMapVisible(),
                content.getMangoPick(),
                content.getMangoPickOrder(),

                content.getCategories()
                        .stream()
                        .map(category -> new FukuokaCategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getNameJa(),
                                category.getSlug(),
                                category.getIcon()
                        ))
                        .toList(),

                images,
                expressions
        );
    }

    public record FukuokaPlaceResponse(
            Long id,
            String name,
            String nameJa,
            String slug
    ) {
    }

    public record FukuokaCategoryResponse(
            Long id,
            String name,
            String nameJa,
            String slug,
            String icon
    ) {
    }

    public record FukuokaContentResponse(
            Long id,
            String title,
            String subtitle,
            String summary,
            String body,
            Long placeId,
            String placeName,
            String placeNameJa,
            String thumbnailImage,
            String heroImage,
            String status,
            java.time.LocalDateTime publishedAt,
            Boolean mapVisible,
            Boolean isMangoPick,
            Integer mangoPickOrder,
            List<FukuokaCategoryResponse> categories,
            List<FukuokaContentImageResponse> images,
            List<JapaneseExpressionResponse> expressions
    ) {
    }
}
