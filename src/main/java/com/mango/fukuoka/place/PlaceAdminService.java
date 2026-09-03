package com.mango.fukuoka.place;

import com.mango.fukuoka.city.City;
import com.mango.fukuoka.city.CityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
@Transactional(
        transactionManager = "fukuokaTransactionManager"
)
public class PlaceAdminService {

    private final FukuokaPlaceRepository placeRepository;
    private final CityRepository cityRepository;
    private static final Logger log =
            LoggerFactory.getLogger(PlaceAdminService.class);

    public PlaceAdminService(
            FukuokaPlaceRepository placeRepository,
            CityRepository cityRepository
    ) {
        this.placeRepository = placeRepository;
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaceAdminResponse> findAll(String citySlug) {
        City city = getCity(citySlug);

        return placeRepository
                .findByCity_SlugOrderByNameAsc(city.getSlug())
                .stream()
                .map(place -> toResponse(
                        place,
                        city.getId(),
                        city.getSlug()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaceAdminResponse findById(
            String citySlug,
            Long id
    ) {
        City city = getCity(citySlug);

        FukuokaPlace place = placeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "장소를 찾을 수 없습니다: " + id
                        )
                );

        if (place.getCity() == null
                || !place.getCity().getId().equals(city.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 도시의 장소가 아닙니다."
            );
        }

        return toResponse(
                place,
                city.getId(),
                city.getSlug()
        );
    }

    public PlaceAdminResponse create(
            String citySlug,
            PlaceAdminRequest request
    ) {
        City city = getCity(citySlug);
        validate(request);

        String slug = normalizeSlug(request.slug());

        if (placeRepository.findBySlug(slug).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 사용 중인 slug입니다: " + slug
            );
        }

        FukuokaPlace place = FukuokaPlace.create(
                city,
                require(request.name(), "장소명을 입력해주세요."),
                request.nameJa(),
                slug,
                request.description(),
                request.latitude(),
                request.longitude(),
                request.address(),
                request.thumbnailImage()
        );

        return toResponse(
                placeRepository.save(place),
                city.getId(),
                city.getSlug()
        );
    }

    public PlaceAdminResponse update(
            String citySlug,
            Long id,
            PlaceAdminRequest request
    ) {
        City city = getCity(citySlug);
        validate(request);

        FukuokaPlace place = placeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "장소를 찾을 수 없습니다: " + id
                        )
                );

        if (place.getCity() == null
                || !place.getCity().getId().equals(city.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 도시의 장소가 아닙니다."
            );
        }

        String slug = normalizeSlug(request.slug());

        if (placeRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 사용 중인 slug입니다: " + slug
            );
        }

        log.debug(
                "[PLACE AFTER UPDATE] latitude={}, longitude={}, address={}",
                place.getLatitude(),
                place.getLongitude(),
                place.getAddress()
        );

        place.update(
                city,
                require(request.name(), "장소명을 입력해주세요."),
                request.nameJa(),
                slug,
                request.description(),
                request.latitude(),
                request.longitude(),
                request.address(),
                request.thumbnailImage()
        );

        placeRepository.save(place);

        log.debug(
                "[PLACE AFTER UPDATE] latitude={}, longitude={}, address={}",
                place.getLatitude(),
                place.getLongitude(),
                place.getAddress()
        );

        return toResponse(
                place,
                city.getId(),
                city.getSlug()
        );
    }

    public void delete(
            String citySlug,
            Long id
    ) {
        City city = getCity(citySlug);

        FukuokaPlace place = placeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "장소를 찾을 수 없습니다: " + id
                        )
                );

        if (place.getCity() == null
                || !place.getCity().getId().equals(city.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 도시의 장소가 아닙니다."
            );
        }

        placeRepository.delete(place);
    }

    private City getCity(String citySlug) {
        String slug = normalizeSlug(citySlug);

        return cityRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "도시를 찾을 수 없습니다: " + slug
                        )
                );
    }

    private void validate(PlaceAdminRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "요청 데이터가 없습니다."
            );
        }

        normalizeSlug(request.slug());

        require(
                request.name(),
                "장소명을 입력해주세요."
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

    private PlaceAdminResponse toResponse(
            FukuokaPlace place,
            Long cityId,
            String citySlug
    ) {
        return new PlaceAdminResponse(
                place.getId(),
                cityId,
                citySlug,
                place.getName(),
                place.getNameJa(),
                place.getSlug(),
                place.getDescription(),
                place.getLatitude(),
                place.getLongitude(),
                place.getAddress(),
                place.getThumbnailImage(),
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }
}