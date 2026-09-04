package com.mango.fukuoka.city;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "city")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_ja", length = 100)
    private String nameJa;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected City() {
    }

    public static City create(
            String slug,
            String name,
            String nameJa,
            String description,
            Boolean active,
            Integer sortOrder
    ) {
        City city = new City();

        city.slug = slug;
        city.name = name;
        city.nameJa = nameJa;
        city.description = description;
        city.active = active;
        city.sortOrder = sortOrder;

        LocalDateTime now = LocalDateTime.now();
        city.createdAt = now;
        city.updatedAt = now;

        return city;
    }

    public void update(
            String slug,
            String name,
            String nameJa,
            String description,
            Boolean active,
            Integer sortOrder
    ) {
        this.slug = slug;
        this.name = name;
        this.nameJa = nameJa;
        this.description = description;
        this.active = active;
        this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getNameJa() {
        return nameJa;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActive() {
        return active;
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
}
