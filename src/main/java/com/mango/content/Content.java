package com.mango.content;

import com.mango.category.SubCategory;
import jakarta.persistence.*;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String korean;

    @Column(nullable = false, length = 100)
    private String japanese;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    protected Content() {
    }

    public Content(
            String korean,
            String japanese,
            String description,
            SubCategory subCategory
    ) {
        this.korean = korean;
        this.japanese = japanese;
        this.description = description;
        this.subCategory = subCategory;
    }


    public Long getId() {
        return id;
    }

    public String getKorean() {
        return korean;
    }

    public String getJapanese() {
        return japanese;
    }

    public String getDescription() {
        return description;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void update(
            String korean,
            String japanese,
            String description,
            SubCategory subCategory
    ) {
        this.korean = korean;
        this.japanese = japanese;
        this.description = description;
        this.subCategory = subCategory;
    }
}