package com.mango.fukuoka.member;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSessionRepository
        extends JpaRepository<MemberSession, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<MemberSession> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);
}
