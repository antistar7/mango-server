package com.mango.fukuoka.place;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.mango.fukuoka.city.City;

@Entity
@Table(name = "place")
public class FukuokaPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_ja", length = 100)
    private String nameJa;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String address;

    @Column(name = "thumbnail_image", length = 500)
    private String thumbnailImage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FukuokaPlace() {
    }

    public static FukuokaPlace create(
            City city,
            String name,
            String nameJa,
            String slug,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            String thumbnailImage
    ) {
        FukuokaPlace place = new FukuokaPlace();

        place.city = city;
        place.name = name;
        place.nameJa = nameJa;
        place.slug = slug;
        place.description = description;
        place.latitude = latitude;
        place.longitude = longitude;
        place.address = address;
        place.thumbnailImage = thumbnailImage;

        LocalDateTime now = LocalDateTime.now();
        place.createdAt = now;
        place.updatedAt = now;

        return place;
    }

    public void update(
            City city,
            String name,
            String nameJa,
            String slug,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            String thumbnailImage
    ) {
        this.city = city;
        this.name = name;
        this.nameJa = nameJa;
        this.slug = slug;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.thumbnailImage = thumbnailImage;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public City getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public String getNameJa() {
        return nameJa;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getThumbnailImage() {
        return thumbnailImage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}