package com.mango.fukuoka.content.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FukuokaContentImageRepository
        extends JpaRepository<FukuokaContentImage, Long> {

    List<FukuokaContentImage> findByContentIdOrderBySortOrderAsc(Long contentId);

    List<FukuokaContentImage> findByContentIdInOrderByContentIdAscSortOrderAsc(
            Collection<Long> contentIds
    );

    void deleteByContentId(Long contentId);
}
