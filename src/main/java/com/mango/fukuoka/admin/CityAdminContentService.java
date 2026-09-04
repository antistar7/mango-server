package com.mango.fukuoka.admin;

import com.mango.fukuoka.category.FukuokaCategory;
import com.mango.fukuoka.category.FukuokaCategoryRepository;
import com.mango.fukuoka.city.City;
import com.mango.fukuoka.city.CityRepository;
import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentRepository;
import com.mango.fukuoka.place.FukuokaPlace;
import com.mango.fukuoka.place.FukuokaPlaceRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(
        transactionManager = "fukuokaTransactionManager"
)
public class CityAdminContentService {

    private final CityRepository cityRepository;
    private final FukuokaContentRepository contentRepository;
    private final FukuokaPlaceRepository placeRepository;
    private final FukuokaCategoryRepository categoryRepository;

    public CityAdminContentService(
            CityRepository cityRepository,
            FukuokaContentRepository contentRepository,
            FukuokaPlaceRepository placeRepository,
            FukuokaCategoryRepository categoryRepository
    ) {
        this.cityRepository = cityRepository;
        this.contentRepository = contentRepository;
        this.placeRepository = placeRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<FukuokaContentResponse> findAll(
            String citySlug
    ) {
        verifyCity(citySlug);

        return contentRepository
                .findByPlace_City_SlugOrderByUpdatedAtDesc(citySlug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FukuokaContentResponse findById(
            String citySlug,
            Long id
    ) {
        verifyCity(citySlug);

        FukuokaContent content = getContent(id);
        verifyContentCity(content, citySlug);

        return toResponse(content);
    }

    public FukuokaContentResponse create(
            String citySlug,
            FukuokaContentRequest request
    ) {
        City city = verifyCity(citySlug);

        FukuokaPlace place =
                findPlaceInCity(request.placeId(), city);

        List<FukuokaCategory> categories =
                findCategories(request.categoryIds());

        FukuokaContent content = FukuokaContent.create(
                request.title(),
                request.subtitle(),
                request.summary(),
                request.body(),
                place,
                request.thumbnailImage(),
                request.heroImage(),
                normalizeStatus(request.status()),
                request.publishedAt(),
                request.mapVisible() != null
                        && request.mapVisible(),
                request.isMangoPick() != null
                        && request.isMangoPick(),
                request.mangoPickOrder() != null
                        ? request.mangoPickOrder()
                        : 0,
                categories
        );

        return toResponse(
                contentRepository.save(content)
        );
    }

    public FukuokaContentResponse update(
            String citySlug,
            Long id,
            FukuokaContentRequest request
    ) {
        City city = verifyCity(citySlug);

        FukuokaContent content = getContent(id);
        verifyContentCity(content, citySlug);

        FukuokaPlace place =
                findPlaceInCity(request.placeId(), city);

        List<FukuokaCategory> categories =
                findCategories(request.categoryIds());

        content.update(
                request.title(),
                request.subtitle(),
                request.summary(),
                request.body(),
                place,
                request.thumbnailImage(),
                request.heroImage(),
                normalizeStatus(request.status()),
                request.publishedAt(),
                request.mapVisible() != null
                        && request.mapVisible(),
                request.isMangoPick() != null
                        && request.isMangoPick(),
                request.mangoPickOrder() != null
                        ? request.mangoPickOrder()
                        : 0,
                categories
        );

        return toResponse(content);
    }

    public void delete(
            String citySlug,
            Long id
    ) {
        verifyCity(citySlug);

        FukuokaContent content = getContent(id);
        verifyContentCity(content, citySlug);

        contentRepository.delete(content);
    }

    private City verifyCity(
            String citySlug
    ) {
        return cityRepository
                .findBySlug(citySlug)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "도시를 찾을 수 없습니다: "
                                        + citySlug
                        )
                );
    }

    private FukuokaContent getContent(
            Long id
    ) {
        return contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "콘텐츠를 찾을 수 없습니다: "
                                        + id
                        )
                );
    }

    private void verifyContentCity(
            FukuokaContent content,
            String citySlug
    ) {
        if (content.getPlace() == null
                || content.getPlace().getCity() == null
                || !citySlug.equals(
                        content.getPlace()
                                .getCity()
                                .getSlug()
                )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 도시의 콘텐츠가 아닙니다."
            );
        }
    }

    private FukuokaPlace findPlaceInCity(
            Long placeId,
            City city
    ) {
        if (placeId == null) {
            return null;
        }

        FukuokaPlace place =
                placeRepository.findById(placeId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "장소를 찾을 수 없습니다: "
                                                + placeId
                                )
                        );

        if (place.getCity() == null
                || !city.getId().equals(
                        place.getCity().getId()
                )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "선택한 장소가 해당 도시의 장소가 아닙니다."
            );
        }

        return place;
    }

    private List<FukuokaCategory> findCategories(
            List<Long> categoryIds
    ) {
        if (categoryIds == null
                || categoryIds.isEmpty()) {
            return List.of();
        }

        List<FukuokaCategory> categories =
                categoryRepository.findAllById(
                        categoryIds
                );

        if (categories.size()
                != categoryIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "존재하지 않는 카테고리가 포함되어 있습니다."
            );
        }

        return categories;
    }

    private String normalizeStatus(
            String status
    ) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }

        String normalized =
                status.toUpperCase();

        if (!normalized.equals("DRAFT")
                && !normalized.equals("PUBLISHED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status는 DRAFT 또는 PUBLISHED만 사용할 수 있습니다."
            );
        }

        return normalized;
    }

    private FukuokaContentResponse toResponse(
            FukuokaContent content
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
                        .map(FukuokaCategory::getId)
                        .toList()
        );
    }
}
