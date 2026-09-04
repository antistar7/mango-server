package com.mango.fukuoka.admin;

import com.mango.fukuoka.category.FukuokaCategory;
import com.mango.fukuoka.category.FukuokaCategoryRepository;
import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentRepository;
import com.mango.fukuoka.place.FukuokaPlace;
import com.mango.fukuoka.place.FukuokaPlaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class FukuokaAdminService {

    private final FukuokaContentRepository contentRepository;
    private final FukuokaPlaceRepository placeRepository;
    private final FukuokaCategoryRepository categoryRepository;

    public FukuokaAdminService(
            FukuokaContentRepository contentRepository,
            FukuokaPlaceRepository placeRepository,
            FukuokaCategoryRepository categoryRepository
    ) {
        this.contentRepository = contentRepository;
        this.placeRepository = placeRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<FukuokaContentResponse> findAll() {
        return contentRepository
                .findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FukuokaContentResponse findById(Long id) {
        return toResponse(getContent(id));
    }

    public FukuokaContentResponse create(
            FukuokaContentRequest request
    ) {
        FukuokaPlace place = findPlace(request.placeId());

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
                request.mapVisible() != null && request.mapVisible(),
                request.isMangoPick() != null && request.isMangoPick(),
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
            Long id,
            FukuokaContentRequest request
    ) {
        FukuokaContent content = getContent(id);

        FukuokaPlace place = findPlace(request.placeId());

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
                request.mapVisible() != null && request.mapVisible(),
                request.isMangoPick() != null && request.isMangoPick(),
                request.mangoPickOrder() != null
                        ? request.mangoPickOrder()
                        : 0,
                categories
        );

        return toResponse(content);
    }

    public void delete(Long id) {
        FukuokaContent content = getContent(id);
        contentRepository.delete(content);
    }

    private FukuokaContent getContent(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "콘텐츠를 찾을 수 없습니다: " + id
                        )
                );
    }

    private FukuokaPlace findPlace(Long placeId) {
        if (placeId == null) {
            return null;
        }

        return placeRepository.findById(placeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "장소를 찾을 수 없습니다: " + placeId
                        )
                );
    }

    private List<FukuokaCategory> findCategories(
            List<Long> categoryIds
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        List<FukuokaCategory> categories =
                categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "존재하지 않는 카테고리가 포함되어 있습니다."
            );
        }

        return categories;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }

        String normalized = status.toUpperCase();

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
