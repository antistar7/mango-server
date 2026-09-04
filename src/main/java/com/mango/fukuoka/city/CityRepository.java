package com.mango.fukuoka.city;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByActiveTrueOrderBySortOrderAsc();

    Optional<City> findBySlugAndActiveTrue(String slug);

    Optional<City> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
