package com.mango.content;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentRepository
        extends JpaRepository<Content, Long> {

    @Override
    @EntityGraph(attributePaths = {"examples"})
    List<Content> findAll();

    @EntityGraph(attributePaths = {"examples"})
    java.util.Optional<Content> findById(Long id);

    @Query("""
            SELECT COALESCE(MAX(c.sortOrder), 0)
            FROM Content c
            WHERE c.subCategory.id = :subCategoryId
            """)
    Integer findMaxSortOrderBySubCategoryId(
            @Param("subCategoryId") Long subCategoryId
    );

    boolean existsBySourceLanguageAndTargetLanguageAndSourceText(
            String sourceLanguage,
            String targetLanguage,
            String sourceText
    );
}