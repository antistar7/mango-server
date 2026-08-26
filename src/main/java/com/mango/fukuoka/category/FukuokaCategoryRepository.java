package com.mango.fukuoka.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FukuokaCategoryRepository
        extends JpaRepository<FukuokaCategory, Long> {

    List<FukuokaCategory> findByActiveTrueOrderBySortOrderAsc();
}