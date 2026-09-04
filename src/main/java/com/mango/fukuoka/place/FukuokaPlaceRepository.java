package com.mango.fukuoka.place;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FukuokaPlaceRepository
        extends JpaRepository<FukuokaPlace, Long> {

    Optional<FukuokaPlace> findBySlug(String slug);

    List<FukuokaPlace> findAllByOrderByNameAsc();

    List<FukuokaPlace> findByCity_SlugOrderByNameAsc(String citySlug);
}