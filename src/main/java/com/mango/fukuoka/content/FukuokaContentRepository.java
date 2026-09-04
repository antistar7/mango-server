package com.mango.fukuoka.content;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FukuokaContentRepository
        extends JpaRepository<FukuokaContent, Long> {

    List<FukuokaContent> findByStatusOrderByPublishedAtDesc(String status);

    List<FukuokaContent> findAllByOrderByUpdatedAtDesc();

    List<FukuokaContent>
    findByPlace_City_SlugAndStatusOrderByPublishedAtDesc(
            String citySlug,
            String status
    );

    List<FukuokaContent> findByPlace_City_SlugOrderByUpdatedAtDesc(
            String citySlug
    );

    @Override
    @EntityGraph(attributePaths = {"place", "categories"})
    java.util.Optional<FukuokaContent> findById(Long id);

}