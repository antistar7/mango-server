package com.mango.fukuoka.member;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_session")
public class MemberSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected MemberSession() {
    }

    public static MemberSession create(
            Member member,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        MemberSession session = new MemberSession();

        session.member = member;
        session.tokenHash = tokenHash;
        session.createdAt = LocalDateTime.now();
        session.expiresAt = expiresAt;

        return session;
    }

    public boolean expired() {
        return !expiresAt.isAfter(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getTokenHash() {
        return tokenHash;
    }
}
