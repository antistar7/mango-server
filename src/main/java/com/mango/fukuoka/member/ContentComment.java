package com.mango.fukuoka.member;

import com.mango.fukuoka.content.FukuokaContent;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_comment")
public class ContentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private FukuokaContent content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 300)
    private String body;

    @Column(nullable = false)
    private Boolean hidden = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ContentComment() {
    }

    public static ContentComment create(
            FukuokaContent content,
            Member member,
            String body
    ) {
        ContentComment comment = new ContentComment();

        comment.content = content;
        comment.member = member;
        comment.body = body;
        comment.hidden = false;
        comment.createdAt = LocalDateTime.now();

        return comment;
    }

    public Long getId() {
        return id;
    }

    public FukuokaContent getContent() {
        return content;
    }

    public Member getMember() {
        return member;
    }

    public String getBody() {
        return body;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
