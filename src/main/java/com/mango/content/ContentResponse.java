package com.mango.content;

public class ContentResponse {

    private final Long id;
    private final String korean;
    private final String japanese;
    private final String description;

    private final Long categoryId;
    private final String categoryName;

    private final Long subCategoryId;
    private final String subCategoryName;

    public ContentResponse(
            Long id,
            String korean,
            String japanese,
            String description,
            Long categoryId,
            String categoryName,
            Long subCategoryId,
            String subCategoryName
    ) {
        this.id = id;
        this.korean = korean;
        this.japanese = japanese;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.subCategoryId = subCategoryId;
        this.subCategoryName = subCategoryName;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }
}