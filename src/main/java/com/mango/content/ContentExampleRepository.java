package com.mango.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentExampleRepository
        extends JpaRepository<ContentExample, Long> {

    List<ContentExample> findByContentIdOrderBySortOrder(Long contentId);
}