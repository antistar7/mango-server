package com.mango.fukuoka.content;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FukuokaContentRepository
        extends JpaRepository<FukuokaContent, Long> {

    List<FukuokaContent> findByStatusOrderByPublishedAtDesc(String status);

    @Override
    @EntityGraph(attributePaths = {"place", "categories"})
    java.util.Optional<FukuokaContent> findById(Long id);
}