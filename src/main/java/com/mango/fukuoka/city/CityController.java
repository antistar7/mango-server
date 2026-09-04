package com.mango.fukuoka.city;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentRepository;
import com.mango.fukuoka.place.FukuokaPlace;
import com.mango.fukuoka.place.FukuokaPlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

    private final CityRepository cityRepository;
    private final FukuokaPlaceRepository placeRepository;
    private final FukuokaContentRepository contentRepository;

    public CityController(
            CityRepository cityRepository,
            FukuokaPlaceRepository placeRepository,
            FukuokaContentRepository contentRepository
    ) {
        this.cityRepository = cityRepository;
        this.placeRepository = placeRepository;
        this.contentRepository = contentRepository;
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

        return contentRepository
                .findByPlace_City_SlugAndStatusOrderByPublishedAtDesc(
                        citySlug,
                        "PUBLISHED"
                )
                .stream()
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
                        content.getMangoPickOrder()
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
            Integer mangoPickOrder
    ) {
    }
}
