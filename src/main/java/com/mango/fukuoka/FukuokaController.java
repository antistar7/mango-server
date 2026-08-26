package com.mango.fukuoka;

import com.mango.fukuoka.category.FukuokaCategoryRepository;
import com.mango.fukuoka.place.FukuokaPlaceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentRepository;
import com.mango.fukuoka.category.FukuokaCategory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fukuoka")
public class FukuokaController {

    private final FukuokaPlaceRepository placeRepository;
    private final FukuokaCategoryRepository categoryRepository;
    private final FukuokaContentRepository contentRepository;

    public FukuokaController(
            FukuokaPlaceRepository placeRepository,
            FukuokaCategoryRepository categoryRepository,
            FukuokaContentRepository contentRepository
    ) {
        this.placeRepository = placeRepository;
        this.categoryRepository = categoryRepository;
        this.contentRepository = contentRepository;
    }

    @GetMapping("/contents")
    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<FukuokaContentResponse> contents() {

        return contentRepository
                .findByStatusOrderByPublishedAtDesc("PUBLISHED")
                .stream()
                .map(content -> new FukuokaContentResponse(
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

                        content.getCategories()
                                .stream()
                                .map(category -> new FukuokaCategoryResponse(
                                        category.getId(),
                                        category.getName(),
                                        category.getNameJa(),
                                        category.getSlug(),
                                        category.getIcon()
                                ))
                                .toList()
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

                content.getCategories()
                        .stream()
                        .map(category -> new FukuokaCategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getNameJa(),
                                category.getSlug(),
                                category.getIcon()
                        ))
                        .toList()
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
            List<FukuokaCategoryResponse> categories
    ) {
    }
}

