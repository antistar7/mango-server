package com.mango.fukuoka.member;

import com.mango.fukuoka.content.FukuokaContent;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_save")
public class MemberSave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private FukuokaContent content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected MemberSave() {
    }

    public static MemberSave create(
            Member member,
            FukuokaContent content
    ) {
        MemberSave save = new MemberSave();

        save.member = member;
        save.content = content;
        save.createdAt = LocalDateTime.now();

        return save;
    }

    public Long getId() {
        return id;
    }

    public FukuokaContent getContent() {
        return content;
    }
}
