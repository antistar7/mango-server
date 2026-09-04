package com.mango.fukuoka.content.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FukuokaContentImageRepository
        extends JpaRepository<FukuokaContentImage, Long> {

    List<FukuokaContentImage> findByContentIdOrderBySortOrderAsc(Long contentId);
}
