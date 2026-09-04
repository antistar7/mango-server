package com.mango.fukuoka.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberSaveRepository extends JpaRepository<MemberSave, Long> {

    @Query("""
            select s.content.id
            from MemberSave s
            where s.member.id = :memberId
            order by s.createdAt desc
            """)
    List<Long> findContentIdsByMemberId(@Param("memberId") Long memberId);

    Optional<MemberSave> findByMember_IdAndContent_Id(
            Long memberId,
            Long contentId
    );

    boolean existsByMember_IdAndContent_Id(Long memberId, Long contentId);

    void deleteByMember_IdAndContent_Id(Long memberId, Long contentId);
}
