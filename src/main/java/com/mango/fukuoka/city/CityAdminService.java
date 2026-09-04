package com.mango.fukuoka.city;

import com.mango.fukuoka.place.FukuokaPlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(
        transactionManager = "fukuokaTransactionManager"
)
public class CityAdminService {

    private final CityRepository cityRepository;
    private final FukuokaPlaceRepository placeRepository;

    public CityAdminService(
            CityRepository cityRepository,
            FukuokaPlaceRepository placeRepository
    ) {
        this.cityRepository = cityRepository;
        this.placeRepository = placeRepository;
    }
    @Transactional(readOnly = true)
    public List<CityAdminResponse> findAll() {
        return cityRepository
                .findAll()
                .stream()
                .sorted(
                        java.util.Comparator
                                .comparing(
                                        City::getSortOrder,
                                        java.util.Comparator.nullsLast(
                                                Integer::compareTo
                                        )
                                )
                                .thenComparing(City::getId)
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CityAdminResponse findById(Long id) {
        return toResponse(getCity(id));
    }

    public CityAdminResponse create(CityAdminRequest request) {
        validate(request);

        String slug = normalizeSlug(request.slug());

        if (cityRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 사용 중인 slug입니다: " + slug
            );
        }

        City city = City.create(
                slug,
                require(request.name(), "도시명을 입력해주세요."),
                request.nameJa(),
                request.description(),
                request.active() != null && request.active(),
                request.sortOrder() != null
                        ? request.sortOrder()
                        : 0
        );

        return toResponse(cityRepository.save(city));
    }

    public CityAdminResponse update(
            Long id,
            CityAdminRequest request
    ) {
        validate(request);

        City city = getCity(id);

        String slug = normalizeSlug(request.slug());

        if (cityRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 사용 중인 slug입니다: " + slug
            );
        }

        city.update(
                slug,
                require(request.name(), "도시명을 입력해주세요."),
                request.nameJa(),
                request.description(),
                request.active() != null && request.active(),
                request.sortOrder() != null
                        ? request.sortOrder()
                        : 0
        );

        return toResponse(city);
    }

    public void delete(Long id) {
        City city = getCity(id);
        cityRepository.delete(city);
    }

    private City getCity(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "도시를 찾을 수 없습니다: " + id
                        )
                );
    }

    private void validate(CityAdminRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "요청 데이터가 없습니다."
            );
        }

        normalizeSlug(request.slug());

        require(
                request.name(),
                "도시명을 입력해주세요."
        );
    }

    private String normalizeSlug(String slug) {
        String value = slug == null
                ? ""
                : slug.trim().toLowerCase();

        if (value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "slug를 입력해주세요."
            );
        }

        return value;
    }

    private String require(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }

        return value.trim();
    }

    private CityAdminResponse toResponse(City city) {
        return new CityAdminResponse(
                city.getId(),
                city.getSlug(),
                city.getName(),
                city.getNameJa(),
                city.getDescription(),
                city.getActive(),
                city.getSortOrder(),
                city.getCreatedAt(),
                city.getUpdatedAt()
        );
    }
}
