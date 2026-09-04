package com.mango.fukuoka.city;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentAssociations;
import com.mango.fukuoka.content.FukuokaContentRepository;
import com.mango.fukuoka.content.expression.JapaneseExpressionResponse;
import com.mango.fukuoka.content.image.FukuokaContentImageResponse;
import com.mango.fukuoka.place.FukuokaPlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

    private final CityRepository cityRepository;
    private final FukuokaPlaceRepository placeRepository;
    private final FukuokaContentRepository contentRepository;
    private final FukuokaContentAssociations contentAssociations;

    public CityController(
            CityRepository cityRepository,
            FukuokaPlaceRepository placeRepository,
            FukuokaContentRepository contentRepository,
            FukuokaContentAssociations contentAssociations
    ) {
        this.cityRepository = cityRepository;
        this.placeRepository = placeRepository;
        this.contentRepository = contentRepository;
        this.contentAssociations = contentAssociations;
    }

    @GetMapping
    public List<CityResponse> cities() {
        return cityRepository
                .findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(city -> new CityResponse(
                        city.getId(),
                        city.getSlug(),
                        city.getName(),
                        city.getNameJa(),
                        city.getDescription()
                ))
                .toList();
    }

    @GetMapping("/{citySlug}/places")
    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<PlaceResponse> places(
            @PathVariable String citySlug
    ) {
        verifyCity(citySlug);

        return placeRepository
                .findByCity_SlugOrderByNameAsc(citySlug)
                .stream()
                .map(place -> new PlaceResponse(
                        place.getId(),
                        place.getName(),
                        place.getNameJa(),
                        place.getSlug(),
                        place.getDescription(),
                        place.getLatitude(),
                        place.getLongitude(),
                        place.getAddress(),
                        place.getThumbnailImage()
                ))
                .toList();
    }

    @GetMapping("/{citySlug}/contents")
    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<ContentResponse> contents(
            @PathVariable String citySlug
    ) {
        verifyCity(citySlug);

        List<FukuokaContent> contents = contentRepository
                .findByPlace_City_SlugAndStatusOrderByPublishedAtDesc(
                        citySlug,
                        "PUBLISHED"
                );

        Map<Long, List<FukuokaContentImageResponse>> images =
                contentAssociations.imagesByContentId(contents);

        Map<Long, List<JapaneseExpressionResponse>> expressions =
                contentAssociations.expressionsByContentId(contents);

        return contents.stream()
                .map(content -> new ContentResponse(
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
                                .map(category -> new CategoryResponse(
                                        category.getId(),
                                        category.getName(),
                                        category.getNameJa(),
                                        category.getSlug(),
                                        category.getIcon()
                                ))
                                .toList(),
                        images.getOrDefault(
                                content.getId(),
                                List.of()
                        ),
                        expressions.getOrDefault(
                                content.getId(),
                                List.of()
                        )
                ))
                .toList();
    }

    private City verifyCity(String citySlug) {
        return cityRepository
                .findBySlugAndActiveTrue(citySlug)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "도시를 찾을 수 없습니다: " + citySlug
                        )
                );
    }

    public record CityResponse(
            Long id,
            String slug,
            String name,
            String nameJa,
            String description
    ) {
    }

    public record PlaceResponse(
            Long id,
            String name,
            String nameJa,
            String slug,
            String description,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude,
            String address,
            String thumbnailImage
    ) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String nameJa,
            String slug,
            String icon
    ) {
    }

    public record ContentResponse(
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
            List<CategoryResponse> categories,
            List<FukuokaContentImageResponse> images,
            List<JapaneseExpressionResponse> expressions
    ) {
    }
}
