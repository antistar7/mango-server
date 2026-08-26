package com.mango.content;

import jakarta.persistence.*;

@Entity
@Table(name = "content_examples")
public class ContentExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false, length = 20)
    private String speaker;

    @Column(name = "source_text", nullable = false, length = 500)
    private String sourceText;

    @Column(name = "target_text", nullable = false, length = 500)
    private String targetText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    protected ContentExample() {
    }

    public ContentExample(
            Content content,
            String speaker,
            String sourceText,
            String targetText,
            Integer sortOrder
    ) {
        this.content = content;
        this.speaker = speaker;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Content getContent() {
        return content;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getTargetText() {
        return targetText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}