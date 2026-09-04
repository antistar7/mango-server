package com.mango.fukuoka.content.image;

import com.mango.fukuoka.content.FukuokaContent;
import jakarta.persistence.*;

@Entity
@Table(name = "content_images")
public class FukuokaContentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private FukuokaContent content;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "image_type", nullable = false, length = 30)
    private String imageType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(length = 500)
    private String caption;

    protected FukuokaContentImage() {
    }

    public Long getId() {
        return id;
    }

    public FukuokaContent getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageType() {
        return imageType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getCaption() {
        return caption;
    }

    public static FukuokaContentImage create(
            FukuokaContent content,
            String imageUrl,
            String imageType,
            Integer sortOrder,
            String caption
    ) {
        FukuokaContentImage created = new FukuokaContentImage();

        created.content = content;
        created.imageUrl = imageUrl;
        created.imageType = imageType;
        created.sortOrder = sortOrder != null ? sortOrder : 0;
        created.caption = caption;

        return created;
    }
}
