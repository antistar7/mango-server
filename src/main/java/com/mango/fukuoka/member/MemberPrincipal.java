package com.mango.fukuoka.member;

/**
 * 공개 회원 인증 주체. 관리자 UserDetails와 섞지 않는다.
 */
public record MemberPrincipal(
        Long id,
        String email,
        String nickname
) {
}
