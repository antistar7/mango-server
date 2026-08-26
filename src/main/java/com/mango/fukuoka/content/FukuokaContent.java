package com.mango.fukuoka.content;

import com.mango.fukuoka.category.FukuokaCategory;
import com.mango.fukuoka.place.FukuokaPlace;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "content")
public class FukuokaContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "LONGTEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private FukuokaPlace place;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "content_category",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<FukuokaCategory> categories = new ArrayList<>();
    @Column(name = "thumbnail_image", length = 500)
    private String thumbnailImage;

    @Column(name = "hero_image", length = 500)
    private String heroImage;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_map_visible", nullable = false)
    private Boolean mapVisible;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FukuokaContent() {
    }

    public List<FukuokaCategory> getCategories() {
        return categories;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public FukuokaPlace getPlace() {
        return place;
    }

    public String getThumbnailImage() {
        return thumbnailImage;
    }

    public String getHeroImage() {
        return heroImage;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Boolean getMapVisible() {
        return mapVisible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}