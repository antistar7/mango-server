package com.mango.fukuoka.content.expression;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface JapaneseExpressionRepository
        extends JpaRepository<JapaneseExpression, Long> {

    List<JapaneseExpression> findByContentIdOrderByIdAsc(Long contentId);

    List<JapaneseExpression> findByContentIdInOrderByContentIdAscIdAsc(
            Collection<Long> contentIds
    );

    void deleteByContentId(Long contentId);

    /**
     * 학습 화면용 조회. 표현이 어느 이야기·도시에서 왔는지 라벨로 보여주기 위해
     * 콘텐츠와 장소·도시를 함께 읽는다. 발행된 콘텐츠의 표현만 학습에 노출한다.
     */
    @Query("""
            SELECT e
            FROM JapaneseExpression e
            JOIN FETCH e.content c
            LEFT JOIN FETCH c.place p
            LEFT JOIN FETCH p.city
            WHERE c.status = 'PUBLISHED'
            ORDER BY c.id ASC, e.id ASC
            """)
    List<JapaneseExpression> findAllPublishedForStudy();

    @Query("""
            SELECT e
            FROM JapaneseExpression e
            JOIN FETCH e.content c
            LEFT JOIN FETCH c.place p
            LEFT JOIN FETCH p.city city
            WHERE c.status = 'PUBLISHED'
              AND city.slug = :citySlug
            ORDER BY c.id ASC, e.id ASC
            """)
    List<JapaneseExpression> findAllPublishedForStudyByCity(
            @Param("citySlug") String citySlug
    );
}
