package com.mango.fukuoka.category;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class FukuokaCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_ja", length = 100)
    private String nameJa;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    protected FukuokaCategory() {
    }

    public Long getId() {
        return id;
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

    public String getIcon() {
        return icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getActive() {
        return active;
    }
}