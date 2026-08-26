package com.mango.content;

import com.mango.category.SubCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @Column(name = "source_language", nullable = false, length = 10)
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;

    @Column(name = "source_text", nullable = false, length = 500)
    private String sourceText;

    @Column(name = "target_text", nullable = false, length = 500)
    private String targetText;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "content",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder ASC")
    private List<ContentExample> examples = new ArrayList<>();

    protected Content() {
    }

    public Content(
            String sourceLanguage,
            String targetLanguage,
            String sourceText,
            String targetText,
            String description,
            Integer difficulty,
            Integer sortOrder,
            SubCategory subCategory
    ) {
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.description = description;
        this.difficulty = difficulty;
        this.sortOrder = sortOrder;
        this.subCategory = subCategory;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getTargetText() {
        return targetText;
    }

    public String getDescription() {
        return description;
    }

    public List<ContentExample> getExamples() {
        return examples;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String sourceLanguage,
            String targetLanguage,
            String sourceText,
            String targetText,
            String description,
            Integer difficulty,
            Integer sortOrder,
            SubCategory subCategory
    ) {
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.description = description;
        this.difficulty = difficulty;
        this.sortOrder = sortOrder;
        this.subCategory = subCategory;
    }
}