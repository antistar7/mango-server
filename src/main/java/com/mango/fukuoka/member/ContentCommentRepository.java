package com.mango.fukuoka.member;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentCommentRepository
        extends JpaRepository<ContentComment, Long> {

    @Override
    @EntityGraph(attributePaths = "member")
    Optional<ContentComment> findById(Long id);

    @EntityGraph(attributePaths = "member")
    List<ContentComment> findByContent_IdAndHiddenFalseOrderByCreatedAtDesc(
            Long contentId
    );
}
