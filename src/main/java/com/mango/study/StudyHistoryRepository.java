package com.mango.study;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyHistoryRepository
        extends JpaRepository<StudyHistory, Long> {

    List<StudyHistory> findByContentIdOrderByStudiedAtDesc(Long contentId);
    List<StudyHistory> findAllByOrderByStudiedAtDesc();
}