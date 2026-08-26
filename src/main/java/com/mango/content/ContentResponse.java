package com.mango.content;

import java.util.List;

public class ContentResponse {

    private final Long id;

    private final String sourceLanguage;
    private final String targetLanguage;

    private final String sourceText;
    private final String targetText;

    private final String description;

    private final Long categoryId;
    private final String categoryName;

    private final Long subCategoryId;
    private final String subCategoryName;

    private final List<ContentExampleResponse> examples;

    public ContentResponse(
            Long id,
            String sourceLanguage,
            String targetLanguage,
            String sourceText,
            String targetText,
            String description,
            Long categoryId,
            String categoryName,
            Long subCategoryId,
            String subCategoryName,
            List<ContentExampleResponse> examples
    ) {
        this.id = id;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.subCategoryId = subCategoryId;
        this.subCategoryName = subCategoryName;
        this.examples = examples;
    }

    public Long getId() {
        return id;
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

    public List<ContentExampleResponse> getExamples() {
        return examples;
    }
}