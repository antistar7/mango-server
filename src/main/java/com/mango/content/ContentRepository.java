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

    /**
     * 학습 화면용 조회. 카테고리 라벨과 예문까지 한 번에 읽어
     * 영속성 컨텍스트를 벗어난 뒤에도 매핑할 수 있게 한다.
     */
    @Query("""
            SELECT DISTINCT c
            FROM Content c
            JOIN FETCH c.subCategory sc
            JOIN FETCH sc.category
            LEFT JOIN FETCH c.examples
            ORDER BY c.sortOrder ASC, c.id ASC
            """)
    List<Content> findAllForStudy();

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